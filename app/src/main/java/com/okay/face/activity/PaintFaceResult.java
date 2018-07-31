package com.okay.face.activity;

import android.graphics.Bitmap;

import com.okay.face.detection.Face;
import com.okay.face.repository.Person;

import java.util.Vector;


public interface PaintFaceResult {
    // 绘制人脸探测结果
    void paintDetctionResult(final Vector<Face> faces, double fps);

    // 绘制人脸比对结果
    void paintVerifyResult(final Person person, double fps);

    // 弹出识别信息用户activity
    void popNewUserActivity(final Bitmap faceBitmap, Face face);
}
