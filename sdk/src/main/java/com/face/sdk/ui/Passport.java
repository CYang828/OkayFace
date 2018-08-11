package com.face.sdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.face.sdk.R;

public class Passport extends View {

    private String name;
    private boolean isClear;

    // 文字画板
    private Paint mPassportTextPaint;
    private Paint mPassportRectPaint;
    private int stroke;

    public Passport(Context context) {
        super(context);
        name = null;
        isClear = false;
        initialize();
    }

    private void initialize() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);

        // 矩形框画板
        mPassportRectPaint = new Paint();
        mPassportRectPaint.setAntiAlias(true);
        mPassportRectPaint.setDither(true);
        mPassportRectPaint.setColor(getResources().getColor(R.color.colorPassport));
        mPassportRectPaint.setStrokeWidth(6);
        mPassportRectPaint.setStyle(Paint.Style.STROKE);

        // 文字画板
        mPassportTextPaint = new Paint();
        mPassportTextPaint.setAntiAlias(true);
        mPassportTextPaint.setDither(true);
        mPassportTextPaint.setStyle(Paint.Style.FILL);
        mPassportTextPaint.setColor(getResources().getColor(R.color.colorPassport));
        mPassportTextPaint.setTextSize(50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isClear && name != null) {
            int textLen = 4;

            if (name.length() + 3 > 4) {
                textLen = name.length() + 3;
            }

            // 计算画框位置
            float left = canvas.getWidth() - (mPassportTextPaint.getTextSize() * textLen) - 50;
            float top = canvas.getHeight() - mPassportTextPaint.getTextSize() * 2 - 35;
            float right = canvas.getWidth() - 30;
            float bottom = canvas.getHeight() - 25;
            RectF passRect = new RectF(left, top, right, bottom);

            canvas.drawRect(passRect, mPassportRectPaint);
            canvas.drawText("验证通过" , (left + 10), (top + 5 + mPassportTextPaint.getTextSize()), mPassportTextPaint);
            canvas.drawText("姓名 - " + name , (left + 10), (top - 5 + mPassportTextPaint.getTextSize() * 2), mPassportTextPaint);
        }
        else {
            RectF rectF = new RectF();
            canvas.drawRect(rectF, mPassportRectPaint);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void show() {
        isClear = false;
        invalidate();
    }

    public void clear() {
        isClear = true;
        invalidate();
    }
}
