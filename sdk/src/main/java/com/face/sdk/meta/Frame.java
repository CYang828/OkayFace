package com.face.sdk.meta;

import android.graphics.Bitmap;
import android.util.Log;


public class Frame {
    private static String TAG = Frame.class.getSimpleName();

    private byte[] frame;
    private int width;
    private int height;
    private int rotate;
    private int scaleWidth;
    private int scaleHeight;
    private Bitmap processedBitmap;
    private float scaleX;
    private float scaleY;

    public Frame(byte[] frame, int width, int height, int rotate, int scaleW, int scaleH) {
        this.frame = frame;
        this.width = width;
        this.height = height;
        this.rotate = rotate;
        this.scaleWidth = scaleW;
        this.scaleHeight = scaleH;

        scaleX = (float) width / (float) scaleW;
        scaleY = (float) height / (float) scaleH;
    }

    public byte[] getFrame() {
        return frame;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRotate() {
        return rotate;
    }

    public int getScaleWidth() {
        return scaleWidth;
    }

    public int getScaleHeight() {
        return scaleHeight;
    }

    public Bitmap getProcessedBitmap() {
        return processedBitmap;
    }

    public void setProcessedBitmap(Bitmap processedBitmap) {
        this.processedBitmap = processedBitmap;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void clear() {
        Log.d(TAG, "clear frame memory");

        if (processedBitmap != null && !processedBitmap.isRecycled()) {
            processedBitmap.recycle();
            processedBitmap = null;
        }

        System.gc();
    }
}
