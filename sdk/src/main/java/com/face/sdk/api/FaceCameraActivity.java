package com.face.sdk.api;

import android.app.AlertDialog;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.face.sdk.R;
import com.face.sdk.error.CameraErrorCallback;
import com.face.sdk.meta.DetectedFaces;
import com.face.sdk.meta.Face;
import com.face.sdk.meta.Person;
import com.face.sdk.ui.FaceBox;
import com.face.sdk.ui.NewUser;
import com.face.sdk.ui.Passport;
import com.face.sdk.utils.Util;

import java.util.List;


public class FaceCameraActivity extends FaceInterfaceActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = FaceCameraActivity.class.getSimpleName();

    // 摄像头显示载体
    SurfaceView mSurfaceView;
    // 摄像头数量
    private int numberOfCameras;
    // 打开摄像头ID
    private int cameraId;
    // 摄像头对象
    private Camera mCamera;
    // 设备的尺寸、角度和方向
    private int mDisplayRotation;
    private int displayOrientation;
    private int previewWidth;
    private int previewHeight;
    private int scaleWidth;
    private int scaleHeight;
    private int screenWidth;
    private int screenHeight;
    // 相机错误回调
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
    private String BUNDLE_CAMERA_ID = "camera";
    // 处理帧数计数
    int captureFrameCount = 0;
    private double cameraFps = 0;
    private double lastCaptureFrameTime = System.currentTimeMillis();

    // 人脸框
    private FaceBox mFaceBox;
    // 新用户注册框是否弹出
    private NewUser newUserWindow;
    private Passport passport;


    public FaceCameraActivity() {
        cameraId = 1;
        setFaceMode(FaceParams.FLAG_FRAME_PREPROCESSING | FaceParams.FLAG_FACE_DETECTION | FaceParams.FLAG_FACE_VERIFICATION);
    }

    /**
     * 初始化接收相机数据的surfaceview
     */
    private void initCameraSurfaceView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
    }

    /**
     * 初始化人脸探测识别框
     */
    private void initFaceBox() {
        mFaceBox = new FaceBox(this);
        addContentView(mFaceBox, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * 初始化新用户注册页面
     */
    private void initNewUserWindow() {
        newUserWindow = new NewUser(this, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 初始化验证后view
     */
    private void initPassport() {
        passport = new Passport(this);
        addContentView(passport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * activity 创建
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.face_camera_activity);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        // 设置全屏模式
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 隐藏掉ActionBar
        getSupportActionBar().hide();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化数据接收的surfaceview
        initCameraSurfaceView();
        initFaceBox();
        initNewUserWindow();
        initPassport();

        if (savedInstanceState != null)
            cameraId = savedInstanceState.getInt(BUNDLE_CAMERA_ID, 0);
    }

    /**
     * 相机切换按钮功能实现
     */
    public void onCameraSwitch(View view) {
        if (numberOfCameras == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Switch Camera").setMessage("Your device have one camera").setNeutralButton("Close", null);
            AlertDialog alert = builder.create();
            alert.show();
        }

        cameraId = (cameraId + 1) % numberOfCameras;
        recreate();
    }

    /**
     * activity启动后检测摄像头权限
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
    }

    /**
     * surfaceview创建时，打开摄像头
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // 查找相机个数
        numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                if (cameraId == 0) cameraId = i;
            }
        }

        mCamera = Camera.open(cameraId);

        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mFaceBox.setFront(true);
        }

        try {
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
        } catch (Exception e) {
            Log.e(TAG, "Could not preview the image.", e);
        }
    }

    /**
     * 适配多尺寸设备进行优化
     */
    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = Util.getOptimalPreviewSize(this, previewSizes, targetRatio);
        previewWidth = previewSize.width;
        previewHeight = previewSize.height;

        // 手动调整尺寸，可以有效的降低app使用内存，预处理、探测帧数也能大幅提高
        // 识别率下降
        //previewWidth = 800;
        //previewHeight = 480;

        /**
         * Calculate size to scale full frame bitmap to smaller bitmap
         * Detect face in scaled bitmap have high performance than full bitmap.
         * The smaller image size -> detect faster, but distance to detect face shorter,
         * so calculate the size follow your purpose
         */
        if (previewWidth / 4 > 360) {
            scaleWidth = 360;
            scaleHeight = 270;
        } else if (previewWidth / 4 > 320) {
            scaleWidth = 320;
            scaleHeight = 240;
        } else if (previewWidth / 4 > 240) {
            scaleWidth = 240;
            scaleHeight = 160;
        } else {
            scaleWidth = 160;
            scaleHeight = 120;
        }

        cameraParameters.setPreviewSize(previewWidth, previewHeight);
        mFaceBox.setPreviewWidth(previewWidth);
        mFaceBox.setPreviewHeight(previewHeight);

        Log.i(TAG, "camera size: " + previewWidth + "x" + previewHeight);
        Log.i(TAG, "scale size: " + scaleWidth + "x" + scaleHeight);
    }

    /**
     * 设置相机自动对焦
     */
    private void setAutoFocus(Camera.Parameters cameraParameters) {
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    /**
     * 设置相机参数
     */
    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        //parameters.setRecordingHint(true);增加采集帧数，但是会让视觉上变卡
        setOptimalPreviewSize(parameters, width, height);
        setAutoFocus(parameters);
        mCamera.setParameters(parameters);
    }

    /**
     * 设置展示的方向和设备的方向一致
     */
    private void setDisplayOrientation() {
        mDisplayRotation = Util.getDisplayRotation(FaceCameraActivity.this);
        displayOrientation = Util.getDisplayOrientation(mDisplayRotation, cameraId);

        mCamera.setDisplayOrientation(displayOrientation);

        if (mFaceBox != null) {
            mFaceBox.setDisplayOrientation(displayOrientation);
        }
    }

    private void setErrorCallback() {
        mCamera.setErrorCallback(mErrorCallback);
    }

    /**
     * 开始预览摄像头画面
     */
    private void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        }
    }

    /**
     * 第一次创建或surfaceview尺寸变更时被调用
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore...
        }

        // 设置相机参数
        configureCamera(width, height);
        // 设置展示的角度
        setDisplayOrientation();
        // 设置错误回调
        setErrorCallback();
        // 开始预览摄像头接收的数据
        startPreview();
    }

    /**
     * 获取摄像头的旋转角度
     */
    public int getCameraRotate() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotate = displayOrientation;

        // 根据设备的旋转角度进行图片调整
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mDisplayRotation % 180 == 0) {
            if (rotate + 180 > 360) {
                rotate = rotate - 180;
            } else
                rotate = rotate + 180;
        }
        return rotate;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        setCameraFps();
        captureFrameCount ++;
        try {
            pullEndToEnd(data, getPreviewWidth(), getPreviewHeight(), getCameraRotate(), getScaleWidth(), getScaleHeight());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.setPreviewCallbackWithBuffer(null);
        mCamera.setErrorCallback(null);
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void detectionResultCallback(final DetectedFaces detectedFaces) {
        super.detectionResultCallback(detectedFaces);

        mFaceBox.setCameraFps(getCameraFps());
        mFaceBox.setPreprocessingDropout(getPreprocessingDropout());
        mFaceBox.setPreprocessingFps(getPreprocessingFps());
        mFaceBox.setDetectFps(getDetectFps());
        mFaceBox.setFaces(detectedFaces.getDetectFaces());

        if (detectedFaces.getDetectFaces().size() > 0) {
            passport.show();
        }
        else {
            passport.clear();
        }
    }

    @Override
    public void verificationNewFaceResultCallback(final Face face) {
        // 弹出用户注册对话框
        super.verificationNewFaceResultCallback(face);
        mFaceBox.setVerifyFps(getVeriyFps());

        if (!newUserWindow.isPop()) {
            newUserWindow.setPop(true);
            newUserWindow.setFace(face);
            newUserWindow.showAsDropDown(screenWidth, 0);
        }
    }

    @Override
    public void verificationOldFaceResultCallback(Person person) {
        // 弹出用户信息框
        super.verificationOldFaceResultCallback(person);
        mFaceBox.setVerifyFps(getVeriyFps());

        passport.setName(person.getName());
    }

    /**
     * 重启相机
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        startPreview();
    }

    /**
     * 停止相机
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * 回收相机资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 相机意外推出
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_CAMERA_ID, cameraId);
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public int getScaleWidth() {
        return scaleWidth;
    }

    public int getScaleHeight() {
        return scaleHeight;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setCameraFps () {
        double currentTime = System.currentTimeMillis();
        cameraFps = 1000 / (currentTime - lastCaptureFrameTime);
        lastCaptureFrameTime = currentTime;
    }

    public double getCameraFps() {
        return cameraFps;
    }
}
