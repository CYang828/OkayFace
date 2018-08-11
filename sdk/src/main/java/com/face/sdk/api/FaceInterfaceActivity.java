package com.face.sdk.api;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import com.face.sdk.detection.FaceDetectThread;
import com.face.sdk.meta.DetectedFaces;
import com.face.sdk.meta.Face;
import com.face.sdk.meta.Frame;
import com.face.sdk.meta.Person;
import com.face.sdk.preprocessing.PreprocessingThread;
import com.face.sdk.verification.FaceVerificationThread;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
 * 人脸SDK对外接口
 */
public abstract class FaceInterfaceActivity extends AppCompatActivity implements FaceResultCallback {

    private static final String TAG = FaceInterfaceActivity.class.getSimpleName();

    private int faceMode = 0x0003;
    private Handler handler;
    private FaceEndToEnd faceEndToEnd;

    public FaceInterfaceActivity() {
    }

    /**
     * 设置face sdk的运行模式
     */
    public void setFaceMode(int faceMode) {
        this.faceMode = faceMode;
    }

    /**
     * 获取与主线程通信的句柄
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * 将帧数据推送到end to end 的管道中
     *
     * @param frame
     * @param frameW
     * @param frameH
     * @throws InterruptedException
     */
    public void pullEndToEnd(byte[] frame, int frameW, int frameH, int rotate, int scaleW, int scaleH) throws InterruptedException {
        Frame frameObject = new Frame(frame, frameW, frameH, rotate, scaleW, scaleH);
        faceEndToEnd.pullPipeline(frameObject);
    }

    /**
     * 获取预处理fps
     */
    public double getPreprocessingFps() {
        return faceEndToEnd.preprocessingThread.getFps();
    }

    /**
     * 获取预处理丢帧率
     */
    public float getPreprocessingDropout() {
        return (float) 1 / faceEndToEnd.preprocessingThread.getInterval();
    }

    /**
     * 获取人脸探测fps
     */
    public double getDetectFps() {
        return faceEndToEnd.faceDetectThread.getFps();
    }

    /**
     * 获取人脸识别fps
     */
    public double getVeriyFps() {
        return faceEndToEnd.faceVerificationThread.getFps();
    }

    /**
     * 在人脸仓库中注册
     */
    public void registerPersonRepository(Person person) {
        faceEndToEnd.faceVerificationThread.getPersonRepository().putInRepo(person);
    }

    /**
     * 人脸端到端流程参数
     */
    public static class FaceParams {
        // 数据预处理
        public static int FLAG_FRAME_PREPROCESSING = 0x0001;

        // 人脸检测
        public static int FLAG_FACE_DETECTION = 0x0002;

        // 人脸对齐
        public static int FLAG_FACE_ALIGNMENT = 0x0004;

        // 人脸分类
        public static int FLAG_FACE_VERIFICATION = 0x0008;

        // 人脸校对
        public static int FLAG_FACE_CLASSIFICATION = 0x0010;

        // 人脸活体检测
        public static int FLAG_FACE_LIVENESS = 0x0020;
    }

    /**
     * 端到端的人脸处理
     */
    public class FaceEndToEnd {

        private final String TAG = FaceEndToEnd.class.getSimpleName();

        public PreprocessingThread preprocessingThread = null;
        public FaceDetectThread faceDetectThread = null;
        public FaceVerificationThread faceVerificationThread = null;

        public FaceEndToEnd(Handler handler, FaceInterfaceActivity ctx) {
            // 根据设置的FaceMode确定端到端流程
            for (int i = 0; i < 16; i++) {
                int flag = (faceMode>>i) & 0x01;

                if (flag == 1) {
                    switch (i) {
                        case 0:
                            // 图像预处理 - preprocessing
                            initPreprocessing(handler, ctx);
                            break;
                        case 1:
                            // 人脸检测 - detection
                            initDetection(handler, ctx);
                            break;
                        case 2:
                            // 人脸对齐 - alignment
                            break;
                        case 3:
                            // 人脸校对 - verification
                            initVerification(handler, ctx);
                            break;
                        case 4:
                            // 人脸分类 - classification
                            break;
                        case 5:
                            // 生命特征检查 - liveness
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        /**
         * 预处理初始化
         */
        private void initPreprocessing(Handler handler, FaceInterfaceActivity ctx) {
            Log.i(TAG, "preprocessing thread load!");
            preprocessingThread = new PreprocessingThread(handler, ctx);
            new Thread(preprocessingThread).start();
        }

        /**
         * 人脸检测初始化
         */
        private void initDetection(Handler handler, FaceInterfaceActivity ctx) {
            Log.i(TAG, "detection thread load!");
            faceDetectThread = new FaceDetectThread(handler, ctx);
            // 将preprocess的出口连接到detect入口上
            preprocessingThread.setOutBridgeEnd(faceDetectThread);
            new Thread(faceDetectThread).start();
        }

        /**
         * 人脸校对初始化
         */
        private void initVerification(Handler handler, FaceInterfaceActivity ctx) {
            Log.i(TAG, "verification thread load!");
            faceVerificationThread = new FaceVerificationThread(handler, ctx);
            faceDetectThread.setOutBridgeEnd(faceVerificationThread);
            new Thread(faceVerificationThread).start();
        }


        /**
         * 将数据放入管道中
         */
        private void pullPipeline(Frame frameObj) throws InterruptedException {
            preprocessingThread.pullInBridge(frameObj);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        faceEndToEnd = new FaceEndToEnd(handler, FaceInterfaceActivity.this);
    }

    @Override
    public void preprocessingResultCallback(final Bitmap preprocessedBitmpa) {
        Log.d(TAG, "preprocessing result callback!");
    }

    @Override
    public void detectionResultCallback(final DetectedFaces detectedFaces) {
        Log.d(TAG, "detection result callback!");
    }

    @Override
    public void verificationNewFaceResultCallback(Face face) {
        Log.d(TAG, "verification new face callback!");
    }

    @Override
    public void verificationOldFaceResultCallback(Person person) {
        Log.d(TAG, "verification old face callback!");
    }
}

