// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.okay.face.activity.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.text.DecimalFormat;
import java.util.Vector;

import com.okay.face.R;
import com.okay.face.detection.Face;


public class FaceOverlayView extends View {
    private float radius = 10.0f;
    private Paint mPaint;
    private Paint mTextPaint;
    private Paint nameTextPaint;
    private Paint mBorderPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private Vector<Face> mFaces;
    private double fps;
    private double cameraFps;
    private float dropout;
    private boolean isFront = false;
    private double detectThreshold = 0.5;
    private float mLineWidth = 50;
    private float mLineHeight = 80;
    private String name = null;


    public FaceOverlayView(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.colorFaceBox));
        mPaint.setStrokeWidth(stroke);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);

        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setDither(true);
        mBorderPaint.setColor(getResources().getColor(R.color.colorFaceBorder));
        mBorderPaint.setStrokeWidth(stroke);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, metrics);
        mTextPaint.setTextSize(size);
        mTextPaint.setColor(getResources().getColor(R.color.colorInformation));
        mTextPaint.setStyle(Paint.Style.FILL);

        nameTextPaint = new Paint();
        nameTextPaint.setAntiAlias(true);
        nameTextPaint.setDither(true);
        nameTextPaint.setTextSize(20);
        nameTextPaint.setColor(getResources().getColor(R.color.colorAccent));
        nameTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setFPS(double fps) {
        this.fps = fps;
    }

    public void setFaces(Vector<Face> faces) {
        mFaces = faces;
        invalidate();
    }

    public void clear() {

    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mFaces != null && mFaces.size() > 0) {

            float scaleX = (float) getWidth() / (float) previewWidth;
            float scaleY = (float) getHeight() / (float) previewHeight;

            switch (mDisplayOrientation) {
                case 90:
                case 270:
                    scaleX = (float) getWidth() / (float) previewHeight;
                    scaleY = (float) getHeight() / (float) previewWidth;
                    break;
            }

            canvas.save();
            canvas.rotate(-mOrientation);

            /** Grid
            final int space = 100;   //长宽间隔
            int vertz = 0;
            int hortz = 0;
            for(int i=0;i<100;i++){
                canvas.drawLine(0,  vertz,  getWidth(), vertz, mPaint);
                canvas.drawLine(hortz, 0, hortz, getHeight(), mPaint);
                vertz+=space;
                hortz+=space;
            }
             */
            Log.i("face size", Integer.toString(mFaces.size()));
            for (Face face : mFaces) {
                if (face.score != detectThreshold){
                    Rect boxRect = face.originalBox;
                    Log.i("Paint width", Integer.toString(canvas.getWidth()));
                    Log.i("Paint height", Integer.toString(canvas.getHeight()));

                    RectF rectF = new RectF(boxRect.left * scaleX,
                                            boxRect.top * scaleY,
                                            boxRect.right * scaleX,
                                            boxRect.bottom * scaleY);

                    if (isFront) {
                        float left = rectF.left;
                        float right = rectF.right;
                        rectF.left = getWidth() - right;
                        rectF.right = getWidth() - left;
                    }

                    canvas.drawRoundRect(rectF, radius, radius, mBorderPaint);

                    CornerPathEffect cornerPathEffect = new CornerPathEffect(radius);
                    mPaint.setPathEffect(cornerPathEffect);
                    Path path = new Path();

                    //左上角
                    path.moveTo(rectF.left, rectF.top + mLineHeight);
                    path.lineTo(rectF.left, rectF.top);
                    path.lineTo(rectF.left + mLineWidth, rectF.top);

                    // 左下角
                    path.moveTo(rectF.left + mLineWidth, rectF.top + rectF.height());
                    path.lineTo(rectF.left, rectF.top + rectF.height());
                    path.lineTo(rectF.left, rectF.top + rectF.height() - mLineHeight);

                    // 右上角
                    path.moveTo(rectF.left + rectF.width() - mLineWidth, rectF.top);
                    path.lineTo(rectF.left + rectF.width(), rectF.top);
                    path.lineTo(rectF.left + rectF.width(), rectF.top + mLineHeight);

                    // 右下角
                    path.moveTo(rectF.left + rectF.width() - mLineWidth, rectF.top + rectF.height());
                    path.lineTo(rectF.left + rectF.width(), rectF.top + rectF.height());
                    path.lineTo(rectF.left + rectF.width(), rectF.top + rectF.height() - mLineHeight);
                    canvas.drawPath(path, mPaint);

                    if (name != null) {
                        canvas.drawText("姓名 " + name, rectF.left + rectF.width()/2 - nameTextPaint.getTextSize() * name.length(), rectF.top - nameTextPaint.getTextSize(), nameTextPaint);
                    }

                    //canvas.drawText("ID " + face.getId(), rectF.left, rectF.bottom + mTextPaint.getTextSize(), mTextPaint);
                    canvas.drawText("人脸探测置信度 " + face.score, rectF.left, rectF.bottom + mTextPaint.getTextSize() * 2, mTextPaint);

                    //canvas.drawText("IOU " + face.iou, rectF.left, rectF.bottom + mTextPaint.getTextSize() * 3, mTextPaint);
                }
            }
            canvas.restore();
        }
        else {
            RectF rectF = new RectF();
            canvas.drawRect(rectF, mPaint);
        }

        DecimalFormat df2 = new DecimalFormat(".##");
        canvas.drawText("摄像头帧数/s: " + df2.format(cameraFps), mTextPaint.getTextSize(), mTextPaint.getTextSize(), mTextPaint);
        canvas.drawText("主动降帧率: " + df2.format(dropout) + "%", mTextPaint.getTextSize(), mTextPaint.getTextSize() * 2, mTextPaint);
        canvas.drawText("人脸探测帧数/s: " + df2.format(fps) + " @ " + previewWidth + "x" + previewHeight, mTextPaint.getTextSize(), mTextPaint.getTextSize() * 3, mTextPaint);
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void setFront(boolean front) {
        isFront = front;
    }

    public void setCameraFps(double cameraFps) {
        this.cameraFps = cameraFps;
    }

    public void setDropout(float dropout) {
        this.dropout = dropout;
    }

    public void setName(String name) {
        this.name = name;
    }
}