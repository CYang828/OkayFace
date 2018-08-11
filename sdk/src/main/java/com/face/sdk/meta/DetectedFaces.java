package com.face.sdk.meta;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

/**
 * 人脸检测到结果结构体
 */
public class DetectedFaces {
    private static String TAG = DetectedFaces.class.getSimpleName();

    private Bitmap originalBitmap;
    private ArrayList<Face> detectFaces;

    public DetectedFaces(Bitmap originalBitmap, ArrayList<Face> detectFaces) {
        this.originalBitmap = originalBitmap;
        this.detectFaces = detectFaces;
    }

    public Bitmap getOriginalBitmap() {
        return originalBitmap;
    }

    public ArrayList<Face> getDetectFaces() {
        return detectFaces;
    }

    public void clear() {
        Log.d(TAG, "clear detect faces memory");

        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
            originalBitmap = null;
        }

        System.gc();
    }
}
