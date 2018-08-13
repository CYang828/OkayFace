package com.face.sdk.ui;

import android.annotation.SuppressLint;
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

import com.face.sdk.R;
import com.face.sdk.meta.Face;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class FaceBox extends View {
    // 人脸框四角角度
    private float radius = 10.0f;
    // 矩形框画板
    private Paint mPaint;
    // 文字画板
    private Paint mTextPaint;
    // 人名画板
    private Paint nameTextPaint;
    // 矩形边框画板
    private Paint mBorderPaint;

    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private int stroke;

    private boolean isFront = false;
    private double detectThreshold = 0.5;
    private float mLineWidth = 50;
    private float mLineHeight = 80;

    private double cameraFps;
    private double preprocessingFps;
    private double preprocessingDropout;
    private double detectFps;
    private double verifyFps;

    private float dropout;
    private ArrayList<Face> mFaces;
    private boolean isShow;

    public FaceBox(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        isShow = false;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);

        // 设置矩形框画板
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.colorFaceBox));
        mPaint.setStrokeWidth(stroke);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);

        // 设置四角画板
        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setDither(true);
        mBorderPaint.setColor(getResources().getColor(R.color.colorFaceBorder));
        mBorderPaint.setStrokeWidth(stroke);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        // 设置文字画板
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, metrics);
        mTextPaint.setTextSize(size);
        mTextPaint.setColor(getResources().getColor(R.color.colorInformation));
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setFPS(double fps) {
        this.detectFps = fps;
    }

    public void setFaces(ArrayList<Face> faces) {
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

            //canvas.save();
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
            for (Face face : mFaces) {
                if (face.score != detectThreshold){
                    Rect boxRect = face.originalBox;

                    @SuppressLint("DrawAllocation") RectF rectF = new RectF(boxRect.left * scaleX,
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

                    canvas.drawText("IOU " + face.getIou(), rectF.left, rectF.bottom + mTextPaint.getTextSize() * 2, mTextPaint);
                    canvas.drawText("人脸探测置信度 " + face.score, rectF.left, rectF.bottom + mTextPaint.getTextSize() * 3, mTextPaint);
                    isShow = true;
                }
                //canvas.restore();
                mFaces = null;
            }
        }

        DecimalFormat df2 = new DecimalFormat(".##");
        canvas.drawText("摄像头帧数/s: " + df2.format(cameraFps), mTextPaint.getTextSize(), mTextPaint.getTextSize(), mTextPaint);
        canvas.drawText("预处理丢帧率/s: " + df2.format(preprocessingDropout), mTextPaint.getTextSize(), mTextPaint.getTextSize() * 2, mTextPaint);
        canvas.drawText("预处理帧数/s: " + df2.format(preprocessingFps), mTextPaint.getTextSize(), mTextPaint.getTextSize() * 3, mTextPaint);
        canvas.drawText("人脸探测帧数/s: " + df2.format(detectFps), mTextPaint.getTextSize(), mTextPaint.getTextSize() * 4, mTextPaint);
        canvas.drawText("人脸比对帧数/s: " + df2.format(verifyFps), mTextPaint.getTextSize(), mTextPaint.getTextSize() * 5, mTextPaint);
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

    public void setPreprocessingFps(double preprocessingFps) {
        this.preprocessingFps = preprocessingFps;
    }

    public void setDetectFps(double detectFps) {
        this.detectFps = detectFps;
    }

    public void setPreprocessingDropout(double preprocessingDropout) {
        this.preprocessingDropout = preprocessingDropout;
    }

    public void setVerifyFps(double verifyFps) {
        this.verifyFps = verifyFps;
    }

    public ArrayList<Face> getmFaces() {
        return mFaces;
    }
}