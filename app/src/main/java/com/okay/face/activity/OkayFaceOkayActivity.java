//package com.okay.face.activity;
//
//import android.app.AlertDialog;
//import android.graphics.Bitmap;
//import android.graphics.drawable.ColorDrawable;
//import android.hardware.Camera;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.v7.widget.DefaultItemAnimator;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.PopupWindow;
//
//import com.okay.face.MainActivity;
//import com.okay.face.R;
//import com.okay.face.activity.ui.FaceOverlayView;
//import com.okay.face.activity.ui.FaceVerifyOverlayView;
//import com.okay.face.adapter.ImagePreviewAdapter;
//import com.okay.face.detection.Face;
//import com.okay.face.detection.FaceDetectThread;
//import com.okay.face.preprocessing.PreprocessingThread;
//import com.okay.face.repository.Person;
//import com.okay.face.utils.CameraErrorCallback;
//import com.okay.face.utils.Util;
//import com.okay.face.verification.FaceVerificationDispatchThread;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Vector;
//
//
//public final class OkayFaceOkayActivity extends OkayFaceActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
//
//    // 日志标识
//    private static final String TAG = OkayFaceOkayActivity.class.getSimpleName();
//    private String BUNDLE_CAMERA_ID = "camera";
//    private static int INIT_CAPTURE_FRAME_INTERVAL = 10;
//
//    private PreprocessingThread preprocessingThread;
//    private FaceDetectThread faceDetectThread;
//    private FaceVerificationDispatchThread faceVerificationDispatchThread;
//
//    // 摄像头数量
//    private int numberOfCameras;
//    // 摄像头对象
//    private Camera mCamera;
//    // 默认的摄像头
//    private int cameraId = 1;
//    private int captureFrameCount =0;
//    private double lastCaptureFrameTime = System.currentTimeMillis();
//    private double cameraFps = 0;
//    private int captureFrameInternal = INIT_CAPTURE_FRAME_INTERVAL;
//
//    // 设备的角度和方向
//    private int mDisplayRotation;
//    private int mDisplayOrientation;
//    private int previewWidth;
//    private int previewHeight;
//    private int prevSettingWidth;
//    private int prevSettingHeight;
//
//    // 使用surface view展示摄像头数据
//    private SurfaceView mSurfaceView;
//
//    // 人脸检测矩形框
//    public FaceOverlayView mFaceOverlayView;
//    // 人脸比对矩形框
//    public FaceVerifyOverlayView mFaceVerifyOverlayView;
//
//    // 相机错误回调
//    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
//
//    // 与UI线程通信的桥
//    private Handler handler;
//
//    // 右侧识别到的人脸crop
//    private RecyclerView recyclerView;
//    private ImagePreviewAdapter imagePreviewAdapter;
//    private ArrayList<Bitmap> facesBitmap;
//
//    // 探测新用户弹出框
//    private static boolean isPopNewUserWindow = false;
//
//    /**
//     * 初始化UI和相关参数
//     */
//    @Override
//    public void onCreate(Bundle icicle) {
//        super.onCreate(icicle);
//        setContentView(R.layout.face_camera_activity);
//
//        // 设置全屏模式
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        // 隐藏掉ActionBar
//        getSupportActionBar().hide();
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        // 初始化SurfaceView对象
//        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
//
//        // 初始化人脸探测识别框
//        mFaceOverlayView = new FaceOverlayView(this);
//        addContentView(mFaceOverlayView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//        // 初始化人脸比对识别框
//        mFaceVerifyOverlayView = new FaceVerifyOverlayView(this);
//        addContentView(mFaceVerifyOverlayView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//        // 初始化右侧识别到的人脸排列布局
//        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//
//        // 初始化线程通信桥
//        handler = new Handler();
//
//        if (icicle != null)
//            cameraId = icicle.getInt(BUNDLE_CAMERA_ID, 0);
//
//        preprocessingThread = new PreprocessingThread();
//        faceDetectThread = new FaceDetectThread(handler, this);
//        faceDetectThread.setPreprocessdeBitmapBridge(preprocessingThread.getPreProcessedFrameBridge());
//
//        faceVerificationDispatchThread = new FaceVerificationDispatchThread(handler, this);
//        faceVerificationDispatchThread.setFaceDetectionBridge(faceDetectThread.getFaceDetectBitmapBridge());
//
//        new Thread(faceDetectThread).start();
//        new Thread(faceVerificationDispatchThread).start();
//    }
//
//
//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        // 检查摄像头权限
//        SurfaceHolder holder = mSurfaceView.getHolder();
//        holder.addCallback(this);
//    }
//
//    /**
//     * 相机切换按钮功能实现
//     */
//    public void onCameraSwitch(View view) {
//        if (numberOfCameras == 1) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Switch Camera").setMessage("Your device have one camera").setNeutralButton("Close", null);
//            AlertDialog alert = builder.create();
//            alert.show();
//        }
//
//        cameraId = (cameraId + 1) % numberOfCameras;
//        recreate();
//    }
//
//    /**
//     * 打开摄像头
//     */
//    @Override
//    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        // 查找相机个数
//        resetData();
//
//        numberOfCameras = Camera.getNumberOfCameras();
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
//            Camera.getCameraInfo(i, cameraInfo);
//            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                if (cameraId == 0) cameraId = i;
//            }
//        }
//
//        mCamera = Camera.open(cameraId);
//
//        Camera.getCameraInfo(cameraId, cameraInfo);
//        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            mFaceOverlayView.setFront(true);
//            mFaceOverlayView.setFront(true);
//        }
//
//        try {
//            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
//        } catch (Exception e) {
//            Log.e(TAG, "Could not preview the image.", e);
//        }
//    }
//
//
//    /**
//     * 设置展示的方向和设备的方向一致
//     */
//    private void setDisplayOrientation() {
//        mDisplayRotation = Util.getDisplayRotation(OkayFaceOkayActivity.this);
//        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, cameraId);
//
//        mCamera.setDisplayOrientation(mDisplayOrientation);
//
//        if (mFaceOverlayView != null) {
//            mFaceOverlayView.setDisplayOrientation(mDisplayOrientation);
//            mFaceVerifyOverlayView.setDisplayOrientation(mDisplayOrientation);
//        }
//    }
//
//
//    /**
//     * 适配多尺寸设备进行优化
//     */
//    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
//        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
//        float targetRatio = (float) width / height;
//        Camera.Size previewSize = Util.getOptimalPreviewSize(this, previewSizes, targetRatio);
//        previewWidth = previewSize.width;
//        previewHeight = previewSize.height;
//
//        /**
//         * Calculate size to scale full frame bitmap to smaller bitmap
//         * Detect face in scaled bitmap have high performance than full bitmap.
//         * The smaller image size -> detect faster, but distance to detect face shorter,
//         * so calculate the size follow your purpose
//         */
//        if (previewWidth / 4 > 360) {
//            prevSettingWidth = 360;
//            prevSettingHeight = 270;
//        } else if (previewWidth / 4 > 320) {
//            prevSettingWidth = 320;
//            prevSettingHeight = 240;
//        } else if (previewWidth / 4 > 240) {
//           prevSettingWidth = 240;
//             prevSettingHeight = 160;
//        } else {
//            prevSettingWidth = 160;
//            prevSettingHeight = 120;
//        }
//
//        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
//        mFaceOverlayView.setPreviewWidth(previewWidth);
//        mFaceOverlayView.setPreviewHeight(previewHeight);
//        mFaceVerifyOverlayView.setPreviewWidth(previewWidth);
//        mFaceVerifyOverlayView.setPreviewHeight(previewHeight);
//    }
//
//
//    /**
//     * 设置相机自动对焦
//     */
//    private void setAutoFocus(Camera.Parameters cameraParameters) {
//        List<String> focusModes = cameraParameters.getSupportedFocusModes();
//        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
//            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//    }
//
//
//    /**
//     * 设置相机参数
//     */
//    private void configureCamera(int width, int height) {
//        Camera.Parameters parameters = mCamera.getParameters();
//        setOptimalPreviewSize(parameters, width, height);
//        setAutoFocus(parameters);
//        mCamera.setParameters(parameters);
//    }
//
//
//    /**
//     * 相机变更时的回调
//     */
//    @Override
//    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
//        if (surfaceHolder.getSurface() == null) {
//            return;
//        }
//
//        try {
//            mCamera.stopPreview();
//        } catch (Exception e) {
//            // Ignore...
//        }
//
//        configureCamera(width, height);
//        setDisplayOrientation();
//        setErrorCallback();
//
//        // 开始预览摄像头接收的数据
//        startPreview();
//    }
//
//
//    /**
//     * 开始预览摄像头画面
//     */
//    private void startPreview() {
//        if (mCamera != null) {
//            mCamera.startPreview();
//            mCamera.setPreviewCallback(this);
//        }
//    }
//
//
//    /**
//     * 重启相机
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        Log.i(TAG, "onResume");
//        startPreview();
//    }
//
//    /**
//     * 停止相机
//     */
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.i(TAG, "onPause");
//        if (mCamera != null) {
//            mCamera.stopPreview();
//        }
//    }
//
//    /**
//     * 回收相机资源
//     */
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        resetData();
//    }
//
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt(BUNDLE_CAMERA_ID, cameraId);
//    }
//
//
//    private void setErrorCallback() {
//        mCamera.setErrorCallback(mErrorCallback);
//    }
//
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//        mCamera.setPreviewCallbackWithBuffer(null);
//        mCamera.setErrorCallback(null);
//        mCamera.release();
//        mCamera = null;
//    }
//
//    /**
//     * 获取摄像头的旋转角度
//     */
//    private int getCameraRotate() {
//        Camera.CameraInfo info = new Camera.CameraInfo();
//        Camera.getCameraInfo(cameraId, info);
//        int rotate = mDisplayOrientation;
//
//        // 根据设备的旋转角度进行图片调整
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mDisplayRotation % 180 == 0) {
//            if (rotate + 180 > 360) {
//                rotate = rotate - 180;
//            } else
//                rotate = rotate + 180;
//        }
//        return rotate;
//    }
//
//    /**
//     * 每一帧的回调，等待面部识别工作完成
//     */
//    @Override
//    public void onPreviewFrame(byte[] _data, Camera _camera) {
//        setCameraFps();
//        captureFrameCount ++;
//        // 主动降低检测的帧数
//        if ( (captureFrameCount % captureFrameInternal) == 0 && preprocessingThread.getPreProcessedFrameBridge().size() < 3) {
//            float xScale = (float) previewWidth / (float) prevSettingWidth;
//            float yScale = (float) previewHeight / (float) prevSettingHeight;
//            faceDetectThread.setScale(xScale, yScale);
//
//            // 调节主动降帧概率
//            captureFrameInternal = (int) (cameraFps / 5);
//            if (captureFrameInternal == 0) {
//                captureFrameInternal = INIT_CAPTURE_FRAME_INTERVAL;
//            }
//
//            // 每一帧都需要获取摄像头的旋转角度
//            // 数据预处理
//            int cameraRotate = getCameraRotate();
//            preprocessingThread.setFrame(_data, previewWidth, previewHeight, cameraRotate);
//            preprocessingThread.setScale(prevSettingWidth, prevSettingHeight);
//            PreprocessingThread.excuteOnPool(preprocessingThread);
//            captureFrameCount = 0;
//        }
//    }
//
//    public void setCameraFps () {
//        double currentTime = System.currentTimeMillis();
//        cameraFps = 1000 / (currentTime - lastCaptureFrameTime);
//        lastCaptureFrameTime = currentTime;
//    }
//
//    /**
//     * 绘制人脸检测后的结果
//     */
//    public void paintDetctionResult(final Vector<Face> faces, final double fps) {
//        handler.post(new Runnable() {
//            public void run() {
//                // 将脸部box发送给FaceOverlayView进行绘制
//                mFaceOverlayView.setFaces(faces);
//                //计算当前脸部识别FPS
//                mFaceOverlayView.setFPS(fps);
//                // 设置摄像头fps
//                mFaceOverlayView.setCameraFps(cameraFps);
//                // 设置主动降帧概率
//                float dropout = 100.0f / captureFrameInternal;
//                mFaceOverlayView.setDropout(dropout);
//            }
//        });
//    }
//
//    /**
//     * 绘制人脸比较后的结果
//     */
//    public void paintVerifyResult(final Person person, final double fps) {
//        handler.post(new Runnable() {
//            public void run() {
//                mFaceOverlayView.setName(person.getName());
//            }
//        });
//    }
//
//    /**
//     * 弹出探测新用户窗口
//     */
//    public void popNewUserActivity(final Bitmap faceBitmap, final Face face) {
//        if (isPopNewUserWindow == false) {
//            isPopNewUserWindow = true;
//            View popupView = LayoutInflater.from(this).inflate(R.layout.activity_detect_new_user, null);
//            final PopupWindow popupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//
//            // 设置imageview为头像图
//            ImageView profileHead = (ImageView) popupView.findViewById(R.id.profile_head_image);
//            profileHead.setAdjustViewBounds(true);
//            profileHead.setImageBitmap(faceBitmap);
//
//            // 设置弹出activity的属性
//            popupWindow.setContentView(popupView);
//            popupWindow.setFocusable(true);
//            popupWindow.setOutsideTouchable(true);
//            // 设置淡入淡出动画
//            popupWindow.setAnimationStyle(R.style.AnimHorizontalIn);
//            popupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPopupWindowBackground)));
//            popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//
//            // 确认后的操作
//            final Button confirmButton = (Button) popupView.findViewById(R.id.profile_head_button);
//            final EditText usernameEditText = (EditText) popupView.findViewById(R.id.username_editview);
//
//            // 确认按钮的监听器
//            confirmButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    popupWindow.dismiss();
//                    String userName = usernameEditText.getText().toString();
//                    Person person = new Person(userName, faceBitmap, face);
//                    MainActivity.personRepository.putInRepo(person);
//                    isPopNewUserWindow = false;
//                }
//            });
//            popupWindow.showAsDropDown(popupView, 0, 0, Gravity.RIGHT);
//        }
//    }
//
//    private void resetData() {
//        if (imagePreviewAdapter == null) {
//            facesBitmap = new ArrayList<>();
//            imagePreviewAdapter = new ImagePreviewAdapter(OkayFaceOkayActivity.this, facesBitmap, new ImagePreviewAdapter.ViewHolder.OnItemClickListener() {
//                @Override
//                public void onClick(View v, int position) {
//                    imagePreviewAdapter.setCheck(position);
//                    imagePreviewAdapter.notifyDataSetChanged();
//                }
//            });
//            recyclerView.setAdapter(imagePreviewAdapter);
//        } else {
//            imagePreviewAdapter.clearAll();
//        }
//    }
//}
