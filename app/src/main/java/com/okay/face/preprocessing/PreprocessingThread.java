package com.okay.face.preprocessing;


import android.graphics.Bitmap;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class PreprocessingThread implements Runnable {

    private static String TAG = PreprocessingThread.class.getSimpleName();
    private static int MAX_FRAME = 10;
    private static int MAX_THREAD = 1;

    private int frameW = 0;
    private int frameH = 0;
    private int scaleW = 0;
    private int scaleH = 0;
    private byte[] frame = null;
    private int frameRotate = 0;
    private double costTime = 0;
    private LinkedBlockingQueue<Bitmap> preProcessedFrameBridge;
    private Bitmap bitmap = null, scaleBitmap = null, rotateBitmap = null;
    private static ExecutorService preProcessingExecutor = Executors.newFixedThreadPool(MAX_THREAD);

    public PreprocessingThread() {
        preProcessedFrameBridge = new LinkedBlockingQueue(MAX_FRAME);
    }

    public static void excuteOnPool(Runnable runnable) {
        preProcessingExecutor.execute(new Thread(runnable));
    }

    /**
     * 进行frame预处理
     */
    @Override
    public void run() {
        long t_start = System.currentTimeMillis();

        // 给frame转换成灰度bitmap
        this.bitmap = FramePreprocess.craeteBitmap(this.frame, this.frameW, this.frameH);

        // 缩放灰度图以提高探测的性能
        this.scaleBitmap = FramePreprocess.scaleBitmap(bitmap, this.scaleW, this.scaleH);

        // 如果图片存在角度上的翻转（主要是指物理设备的角度），则将bitmap翻转回来
        if (frameRotate != 0) {
            this.rotateBitmap = FramePreprocess.rotateBitmap(scaleBitmap, frameRotate);
            this.scaleBitmap = this.rotateBitmap;
        } else {
            this.rotateBitmap = null;
        }

        // 将处理完的数据放入preProcessedFrameQueue队列中
        try {
            this.preProcessedFrameBridge.put(this.scaleBitmap);
            Log.i(TAG, "Queue size: " + Integer.toString(this.preProcessedFrameBridge.size()));
            Log.i(TAG, this.preProcessedFrameBridge.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.costTime = System.currentTimeMillis() - t_start;
        Log.i(TAG, "Preprocessing Cost Time: " + Double.toString(this.costTime));
    }

    public void setFrame(byte[] frame, int frameW, int frameH, int frameRotate) {
        this.frame = frame;
        this.frameW = frameW;
        this.frameH = frameH;
        this.frameRotate = frameRotate;
    }

    public void setScale(int scaleW, int scaleH) {
        this.scaleW = scaleW;
        this.scaleH = scaleH;
    }

    public LinkedBlockingQueue getPreProcessedFrameBridge() {
        return preProcessedFrameBridge;
    }
}
