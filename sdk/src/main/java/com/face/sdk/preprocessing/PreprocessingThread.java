package com.face.sdk.preprocessing;


import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.face.sdk.api.FaceInterfaceActivity;
import com.face.sdk.meta.Frame;
import com.face.sdk.scaffold.BridgeRunnable;


public class PreprocessingThread<T extends FaceInterfaceActivity> extends BridgeRunnable<Frame, Frame, T> {

    private final static String TAG = PreprocessingThread.class.getSimpleName();

    public PreprocessingThread(Handler handler, T ctx) {
        super(handler, ctx);
    }

    @Override
    public void run() {
        Frame frameObj = null;
        Bitmap bitmap;
        Bitmap scaleBitmap;
        Bitmap rotateBitmap;
        Bitmap preprocessedBitmap;

        while (!isFinished) {
            try {
                frameObj = inBridge.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long t_start = System.currentTimeMillis();
            // 将frame转换成灰度bitmap
            bitmap = FramePreprocess.craeteBitmap(frameObj.getFrame(), frameObj.getWidth(), frameObj.getHeight(), true);

            // 缩放灰度图以提高探测的性能
            scaleBitmap = FramePreprocess.scaleBitmap(bitmap, frameObj.getScaleWidth(), frameObj.getScaleHeight());

            // 如果图片存在角度上的翻转（主要是指物理设备的角度），则将bitmap翻转回来
            if (frameObj.getRotate() != 0) {
                rotateBitmap = FramePreprocess.rotateBitmap(scaleBitmap, frameObj.getRotate());
                preprocessedBitmap = rotateBitmap;
            } else {
                preprocessedBitmap = scaleBitmap;
            }

            final Bitmap postPreprocessedBitmap = preprocessedBitmap;
            lastCostTime = System.currentTimeMillis() - t_start;
            Log.d(TAG, "preprocessing cost time: " + Double.toString(lastCostTime));

            // 通知预处理结果
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ctx.preprocessingResultCallback(postPreprocessedBitmap);
                }
            });

            frameObj.setProcessedBitmap(preprocessedBitmap);

            if (outBridge != null) {
                // 将处理后的结果放入桥出口队列中
                try {
                    outBridgeEnd.pullInBridge(frameObj);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "preprocess finished and put it to out bridge, bridge queue size: " + Integer.toString(outBridge.size()));
            }
            else {
                frameObj.clear();
            }
        }
    }

    @Override
    public int setInterval() {
        interval = 5;
        return interval;
    }

    public double getFps() {
        return 1000 / lastCostTime;
    }
}
