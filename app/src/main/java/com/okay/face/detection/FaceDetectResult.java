package com.okay.face.detection;

import android.graphics.Bitmap;

import java.util.Vector;


public class FaceDetectResult {
    private Bitmap originalBitmap;
    private Vector<Face> detectFaces;

    public FaceDetectResult(Bitmap originalBitmap, Vector<Face> detectFaces) {
        this.originalBitmap = originalBitmap;
        this.detectFaces = detectFaces;
    }

    public Bitmap getOriginalBitmap() {
        return originalBitmap;
    }

    public Vector<Face> getDetectFaces() {
        return detectFaces;
    }
}
