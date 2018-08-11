package com.face.sdk.api;

import android.graphics.Bitmap;

import com.face.sdk.meta.DetectedFaces;

import com.face.sdk.meta.Face;
import com.face.sdk.meta.Person;


public interface FaceResultCallback {

    // 预处理结果回调函数
    void preprocessingResultCallback(final Bitmap preprocessedBitmpa);

    // 人脸探测结果回调函数
    void detectionResultCallback(final DetectedFaces detectedFaces);

    // 人脸比对结果新脸回调函数
    void verificationNewFaceResultCallback(final Face face);

    // 人脸比对结果已存在脸回调函数
    void verificationOldFaceResultCallback(final Person person);

    // 弹出识别信息用户activity
    //void popNewUserActivity(final Bitmap faceBitmap, Face face);

}
