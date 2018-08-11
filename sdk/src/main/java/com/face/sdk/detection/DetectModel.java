package com.face.sdk.detection;

import android.graphics.Bitmap;

import com.face.sdk.meta.DetectedFaces;

public interface DetectModel {

    DetectedFaces detectFaces(Bitmap detectBitmap, int detectMiniFaceSize);
}
