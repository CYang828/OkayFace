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

import com.okay.face.R;
import com.okay.face.detection.Face;
import com.okay.face.repository.Person;

import java.text.DecimalFormat;


public class FaceVerifyOverlayView extends View {

    private float radius = 10.0f;
    private Paint mPaint;
    private Paint mTextPaint;
    private Paint mBorderPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private Person person = null;
    private double fps;
    private double cameraFps;
    private float dropout;
    private boolean isFront = false;
    private float mLineWidth = 50;
    private float mLineHeight = 80;

    public FaceVerifyOverlayView(Context context) {
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
        mBorderPaint.setColor(getResources().getColor(R.color.colorAccent));
        mBorderPaint.setStrokeWidth(stroke);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, metrics);
        mTextPaint.setTextSize(size);
        mTextPaint.setColor(getResources().getColor(R.color.colorInformation));
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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

        if (person != null) {
            Face face = person.getFace();

            Rect boxRect = face.originalBox;
            Log.i("verify Paint width", Integer.toString(canvas.getWidth()));

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

            //canvas.drawText("ID " + face.getId(), rectF.left, rectF.bottom + mTextPaint.getTextSize(), mTextPaint);
            canvas.drawText("人脸探测置信度 " + face.score, rectF.left, rectF.bottom + mTextPaint.getTextSize() * 2, mTextPaint);
            canvas.drawText("姓名 " + person.getName(), rectF.left, rectF.bottom + mTextPaint.getTextSize() * 3, mTextPaint);
            canvas.restore();

            DecimalFormat df2 = new DecimalFormat(".##");
            canvas.drawText("摄像头帧数/s: " + df2.format(cameraFps), mTextPaint.getTextSize(), mTextPaint.getTextSize(), mTextPaint);
            canvas.drawText("主动降帧率: " + df2.format(dropout) + "%", mTextPaint.getTextSize(), mTextPaint.getTextSize() * 2, mTextPaint);
            canvas.drawText("人脸探测帧数/s: " + df2.format(fps) + " @ " + previewWidth + "x" + previewHeight, mTextPaint.getTextSize(), mTextPaint.getTextSize() * 3, mTextPaint);
        }
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

    public void setFPS(double fps) {
        this.fps = fps;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setPerson(Person person) {
        this.person = person;
        invalidate();
    }
}
