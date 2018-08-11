package com.face.sdk.detection;


import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.face.sdk.api.FaceInterfaceActivity;
import com.face.sdk.meta.DetectedFaces;
import com.face.sdk.meta.Face;
import com.face.sdk.meta.Frame;
import com.face.sdk.scaffold.BridgeRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 人脸探测线程
 */
public class FaceDetectThread<T extends FaceInterfaceActivity> extends BridgeRunnable<Frame, DetectedFaces, T> {

    private static String TAG = FaceDetectThread.class.getSimpleName();

    // 可检测到最小人脸
    private int detectMiniFaceSize;

    // 人脸检测模型
    private MTCNN detectModel;


    public FaceDetectThread(Handler handler, T ctx) {
        super(handler, ctx);
        detectMiniFaceSize = DetectParams.DETECT_MINI_FACE_SIZE;
        // 加载模型
        detectModel = new MTCNN(ctx);
    }

    @Override
    public void run() {
        while (!isFinished) {
            Frame frameOjb = null;
            final DetectedFaces detectedFaces;

            try {
                frameOjb = inBridge.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long t_start = System.currentTimeMillis();

            // 使用探测模型获取探测后的数据
            detectedFaces = detectModel.detectFaces(frameOjb.getProcessedBitmap(), detectMiniFaceSize);

            // 计算识别原图上人脸框的位置
            for (Face face: detectedFaces.getDetectFaces()) {
                face.transform2OriginalBox(frameOjb.getScaleX(), frameOjb.getScaleY());
            }

            lastCostTime = System.currentTimeMillis() - t_start;
            Log.d(TAG, "detect face cost time: " + Double.toString(lastCostTime));


            // 通知人脸探测结果
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ctx.detectionResultCallback(detectedFaces);
                }
            });

            if (detectedFaces.getDetectFaces().size() > 0) {
                if (outBridge != null) {
                    // 将人脸检测的结果放入桥出口队列中
                    try {
                        outBridgeEnd.pullInBridge(detectedFaces);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (outBridge == null) {
                // 清空帧对象
                frameOjb.clear();
            }
        }
    }

    public double getFps() {
        return 1000 / lastCostTime;
    }
}
