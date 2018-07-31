package com.okay.face.activity.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.ArrayList;

import com.okay.face.R;


/**
 * funtion :
 * author  :smallbluewhale.
 * date    :2017/9/21.
 * version :1.0.
 */

/*
* 三角形颜色是不变的，圆形的线框颜色从蓝色变成白色
* 动画总共有四个过程
* 1.整体先稍微变小
* 2.开始转圈圈
* 3.整体变小，并且颜色变成白色，然后开始显示白色
* 4.逆时针转动
* */
public class FaceDetectView extends SurfaceView implements SurfaceHolder.Callback, Animator.AnimatorListener {
    private Context context;
    /*
        * 人脸识别相关定义类
        * */
    private Bitmap bitmap;
    private FaceDetector faceDetector;
    private SparseArray<Face> faceSparseArray;
    private Frame frame;
    private float outSideRadiu;                                 //圆的半径
    private float innerRadiu;
    private float innerInnerRadiu;
    private float xScale , yScale;                                        //x , y缩放的比例

    private int innerAlph;                                    //外圈透明度
    private int outSideAlph;                                    //外圈透明度
    private static final float CIRCLERADIU = 200;               //圆固定的半径
    private Point circleCenterPoint;                            //圆心位置
    private Point rectanglePoint1, rectanglePoint2;             //三角形的中心点位置
    private Point tanglePoint1, tanglePoint2, tanglePoint3;
    private Point tanglePoint4, tanglePoint5, tanglePoint6;


    private int outSideCircleStarAngle;                              //起点的角度
    private int innerCircleStarAngle;                               //起点的角度
    private int startAngle;                              //起点的角度
    private int outSideGapAngle;                          //外圆间隔
    private int innerGapAngle;                          //内圆间隔
    private float nameSize;                                     //名字字体大小
    private int drawableID = R.mipmap.ic_launcher;            //动画完成后，显示的图片
    private int duration = 500;                               //动画时间
    /*
    * 圆的部分
    * */
    private Paint bcPaint;           //圆的画笔
    private RectF outSideCircleRectF;           //最外圆矩形

    private Paint innerCirclePaint;             //内圆的画笔
    private RectF innerCircleRectF;             //内圆矩形

    private Paint innerInnerCirclePaint;        //内内圆的画笔
    private RectF innerInnerCircleRectF;        //内内圆矩形

    private Paint rectanglePaint;       //两个三角形的画笔


    private Path rectanglePath1;
    private Path rectanglePath2;



    private AnimatorSet bcScaleAnimatorSet;             //blue circle Scale
    private AnimatorSet bcRotateAnimatorSet;            //blue circle Rotate
    private AnimatorSet bcAlphAnimatorSet;              //blue circle Alph
    private AnimatorSet innerRotateAnimatorSet;         //inner circle Rotate
    private AnimatorSet innerAlphAnimatorSet;           //inner circle Alph
    private AnimatorSet innerScaleAnimatorSet;          //inner circle Scale

    private ObjectAnimator bcScaleAnimator1;
    private ObjectAnimator bcScaleAnimator2;
    private ObjectAnimator bcScaleAnimator3;
    private ObjectAnimator bcScaleAnimator4;

    private ObjectAnimator bcAlphAnimator1;

    private ObjectAnimator bcRotateAnimation;
    /*
    * 内圈
    * */
    private ObjectAnimator innerScaleAnimation1;
    private ObjectAnimator innerScaleAnimation2;
    private ObjectAnimator innerScaleAnimation3;
    private ObjectAnimator innerScaleAnimation4;
    private ObjectAnimator innerAlphAnimation;
    private ObjectAnimator innerRotateAnimation;

    private SurfaceHolder mSurfaceHolder;   //绘图的canvas
    private Canvas canvas;
    private boolean isAnimationEnd;
    private ArrayList<Animator> animatorArrayList;

    public FaceDetectView(Context context) {
        super(context);
    }

    public FaceDetectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceDetectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
//        initFaceDetectorData();
        initPaint();
        initView();
        initData();
    }

    private void initFaceDetectorData() {
        faceDetector = new FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS).build();
        bitmap = BitmapFactory.decodeResource(getResources() , 0);
        if(faceDetector.isOperational() && bitmap!=null){
            frame = new Frame.Builder().setBitmap(bitmap).build();
            faceSparseArray = faceDetector.detect(frame);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //设置长宽
        setMeasuredDimension(getFaceDetectViewWidth(widthMeasureSpec), getFaceDetectViewHeight(heightMeasureSpec));
        if(bitmap!= null) {
            xScale = getMeasuredWidth() / bitmap.getWidth();
            yScale = getMeasuredHeight() / bitmap.getHeight();
        }
    }


    private int getFaceDetectViewHeight(int heightMeasureSpec) {
        int height = 0;
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            height = specSize;
        } else {
            height = (int) (3 * outSideRadiu);
            if (specMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, specSize);
            }
        }
        return height;
    }

    private int getFaceDetectViewWidth(int widthMeasureSpec) {
        int width = 0;
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            width = specSize;
        } else {
            width = (int) (3 * outSideRadiu);
            if (specMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, specSize);
            }
        }
        return width;
    }

    private void initPaint() {
        //圆形画笔
        bcPaint = new Paint();
        bcPaint.setAntiAlias(true);
        bcPaint.setStyle(Paint.Style.STROKE);
        bcPaint.setColor(Color.BLUE);
        bcPaint.setStrokeWidth(2);

        innerCirclePaint = new Paint();
        innerCirclePaint.setAntiAlias(true);
        innerCirclePaint.setStyle(Paint.Style.STROKE);
        innerCirclePaint.setStrokeWidth(2);
        innerCirclePaint.setColor(Color.BLACK);

        innerInnerCirclePaint = new Paint();
        innerInnerCirclePaint.setAntiAlias(true);
        innerInnerCirclePaint.setStyle(Paint.Style.STROKE);
        innerInnerCirclePaint.setStrokeWidth(2);
        innerInnerCirclePaint.setColor(Color.GRAY);

        rectanglePaint = new Paint();
        rectanglePaint.setAntiAlias(true);
        rectanglePaint.setStyle(Paint.Style.FILL);
        rectanglePaint.setStrokeWidth(10);
        rectanglePaint.setColor(Color.YELLOW);

    }

    public FaceDetectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    private void initView() {
        mSurfaceHolder = getHolder();
        //绑定回调方法
        mSurfaceHolder.addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        setFocusable(true);
        setKeepScreenOn(true);
        setFocusableInTouchMode(true);      //保持屏幕不休眠
    }

    private void initData() {
        setBackgroundResource(0);
        animatorArrayList = new ArrayList<Animator>();
        outSideGapAngle = 10;
        innerGapAngle = 10;
        outSideRadiu = CIRCLERADIU;
        outSideCircleRectF = new RectF();
        innerCircleRectF = new RectF();
        innerInnerCircleRectF = new RectF();
        rectanglePath1 = new Path();
        rectanglePath2 = new Path();
        circleCenterPoint = new Point();
        rectanglePoint1 = new Point();
        rectanglePoint2 = new Point();
        tanglePoint1 = new Point();
        tanglePoint2 = new Point();
        tanglePoint3 = new Point();
        tanglePoint4 = new Point();
        tanglePoint5 = new Point();
        tanglePoint6 = new Point();
        circleCenterPoint.x = (int) outSideRadiu * 3 / 2;
        circleCenterPoint.y = (int) outSideRadiu * 3 / 2;

        initArcData();


        if (null == bcScaleAnimator1) {
            bcScaleAnimator1 = ObjectAnimator.ofFloat(this, "outSideRadiu", 0f, 1.04f);
            bcScaleAnimator1.setDuration(200);
            bcScaleAnimator1.setInterpolator(new LinearInterpolator());
        }

        if (null == bcAlphAnimator1) {
            bcAlphAnimator1 = ObjectAnimator.ofInt(this, "outSideAlph", 0, 255);
            bcScaleAnimator1.setDuration(200);
            bcScaleAnimator1.setInterpolator(new LinearInterpolator());
        }


        if (null == bcScaleAnimator2) {
            bcScaleAnimator2 = ObjectAnimator.ofFloat(this, "outSideRadiu", 1.04f, 1f);
            bcScaleAnimator2.setDuration(66);
            bcScaleAnimator2.setInterpolator(new LinearInterpolator());
        }

        if (null == bcScaleAnimator3) {
            bcScaleAnimator3 = ObjectAnimator.ofFloat(this, "outSideRadiu", 1f, 1.02f);
            bcScaleAnimator3.setDuration(66);
            bcScaleAnimator3.setInterpolator(new LinearInterpolator());
        }

        if (null == bcScaleAnimator4) {
            bcScaleAnimator4 = ObjectAnimator.ofFloat(this, "outSideRadiu", 1.02f, 1f);
            bcScaleAnimator4.setDuration(66);
            bcScaleAnimator4.setInterpolator(new LinearInterpolator());
        }

        if(null == bcScaleAnimatorSet){
            bcScaleAnimatorSet = new AnimatorSet();
            animatorArrayList.clear();
            animatorArrayList.add(bcScaleAnimator1);
            animatorArrayList.add(bcScaleAnimator2);
            animatorArrayList.add(bcScaleAnimator3);
            animatorArrayList.add(bcScaleAnimator4);
            bcScaleAnimatorSet.playSequentially(animatorArrayList);
        }

        if (null == bcRotateAnimation) {
            bcRotateAnimation = ObjectAnimator.ofInt(this, "outSideCircleStarAngle", 0, 360);
            bcRotateAnimation.setDuration(866);
            bcRotateAnimation.setInterpolator(new LinearInterpolator());
        }


        if(null == bcAlphAnimatorSet){
            bcAlphAnimatorSet = new AnimatorSet();
            animatorArrayList.clear();
            animatorArrayList.add(bcAlphAnimator1);
            bcAlphAnimatorSet.playSequentially(animatorArrayList);
        }

        if(null == bcRotateAnimatorSet){
            bcRotateAnimatorSet = new AnimatorSet();
            animatorArrayList.clear();
            animatorArrayList.add(bcRotateAnimation);
            bcRotateAnimatorSet.playSequentially(animatorArrayList);
            bcAlphAnimator1.start();
            bcRotateAnimatorSet.setStartDelay(800);
        }

        /*
        * 外圈大小
        * */
        if (null == innerScaleAnimation1) {
            innerScaleAnimation1 = ObjectAnimator.ofFloat(this, "innerRadiu", 0f, 1.04f);
            innerScaleAnimation1.setDuration(200);
            innerScaleAnimation1.setInterpolator(new LinearInterpolator());
        }

        if (null == innerScaleAnimation2) {
            innerScaleAnimation2 = ObjectAnimator.ofFloat(this, "innerRadiu", 1.04f , 1f);
            innerScaleAnimation2.setDuration(134);
            innerScaleAnimation2.setInterpolator(new LinearInterpolator());
        }

        if (null == innerScaleAnimation3) {
            innerScaleAnimation3 = ObjectAnimator.ofFloat(this, "innerRadiu", 1f, 1.02f );
            innerScaleAnimation3.setDuration(66);
            innerScaleAnimation3.setInterpolator(new LinearInterpolator());
        }

        if (null == innerScaleAnimation4) {
            innerScaleAnimation4 = ObjectAnimator.ofFloat(this, "innerRadiu", 1.02f , 1f);
            innerScaleAnimation4.setDuration(66);
            innerScaleAnimation4.setInterpolator(new LinearInterpolator());
        }

        if(null == innerScaleAnimatorSet){
            innerScaleAnimatorSet = new AnimatorSet();
            animatorArrayList.clear();
            animatorArrayList.add(innerScaleAnimation1);
            animatorArrayList.add(innerScaleAnimation2);
            animatorArrayList.add(innerScaleAnimation3);
            animatorArrayList.add(innerScaleAnimation4);
            innerScaleAnimatorSet.playSequentially(animatorArrayList);
            innerScaleAnimatorSet.setStartDelay(34);
        }

        /*
        * 透明度
        * */
        if (null == innerAlphAnimation) {
            innerAlphAnimation = ObjectAnimator.ofInt(this, "innerAlph", 0 , 255);
            innerAlphAnimation.setDuration(200);
            innerAlphAnimation.setInterpolator(new LinearInterpolator());
        }


        if(null == innerAlphAnimatorSet){
            innerAlphAnimatorSet = new AnimatorSet();
            animatorArrayList.clear();
            animatorArrayList.add(innerAlphAnimation);
            innerAlphAnimatorSet.playSequentially(animatorArrayList);
            innerAlphAnimatorSet.setStartDelay(34);
        }

        /*
        * 旋转角度
        * */
        if (null == innerRotateAnimation) {
            innerRotateAnimation = ObjectAnimator.ofInt(this, "innerCircleStarAngle", 0, 360);
            innerRotateAnimation.setDuration(866);
            innerRotateAnimation.setInterpolator(new LinearInterpolator());
        }

        if(null == innerRotateAnimatorSet){
            innerRotateAnimatorSet = new AnimatorSet();
            animatorArrayList.clear();
            animatorArrayList.add(innerRotateAnimation);
            innerRotateAnimatorSet.playSequentially(animatorArrayList);
            innerRotateAnimatorSet.setStartDelay(800);
        }
    }

    public void setInnerRadiu(float innerRadiu) {
        this.innerRadiu = innerRadiu;
        draw();
    }

    public void setInnerCircleStarAngle(int innerCircleStarAngle) {
        this.innerCircleStarAngle = 360 - innerCircleStarAngle;
        draw();
    }

    public void setInnerAlph(int innerAlph) {
        this.innerAlph = innerAlph;
        draw();
    }

    public void setOutSideAlph(int outSideAlph) {
        this.outSideAlph = outSideAlph;
        draw();
    }

    public void setOutSideCircleStarAngle(int outSideCircleStarAngle) {
        this.outSideCircleStarAngle = outSideCircleStarAngle;
        draw();
    }

    public void setOutSideRadiu(float outSideRadiu) {
        this.outSideRadiu = CIRCLERADIU * outSideRadiu;
        draw();
    }



    private void initArcData() {
        innerRadiu = outSideRadiu * 0.7f;
        innerInnerRadiu = outSideRadiu * 0.2f;
        /*
        * 设置三角形的数据
        * */
        rectanglePoint1 = caculateRectanglePosition(circleCenterPoint, outSideCircleStarAngle, outSideRadiu);
        rectanglePoint2 = caculateRectanglePosition(circleCenterPoint, outSideCircleStarAngle + 180, outSideRadiu);

        tanglePoint1 = caculateRectanglePosition(rectanglePoint1, outSideCircleStarAngle + 180, outSideRadiu * 0.1f);
        tanglePoint2 = caculateRectanglePosition(rectanglePoint1, outSideCircleStarAngle + 120 + 180, outSideRadiu * 0.1f);
        tanglePoint3 = caculateRectanglePosition(rectanglePoint1, outSideCircleStarAngle + 240 + 180, outSideRadiu * 0.1f);
        rectanglePath1.reset();
        rectanglePath1.moveTo(tanglePoint1.x, tanglePoint1.y);
        rectanglePath1.lineTo(tanglePoint2.x, tanglePoint2.y);
        rectanglePath1.lineTo(tanglePoint3.x, tanglePoint3.y);
        rectanglePath1.close();

        tanglePoint4 = caculateRectanglePosition(rectanglePoint2, outSideCircleStarAngle, outSideRadiu * 0.1f);
        tanglePoint5 = caculateRectanglePosition(rectanglePoint2, outSideCircleStarAngle + 120, outSideRadiu * 0.1f);
        tanglePoint6 = caculateRectanglePosition(rectanglePoint2, outSideCircleStarAngle + 240, outSideRadiu * 0.1f);
        rectanglePath2.reset();
        rectanglePath2.moveTo(tanglePoint4.x, tanglePoint4.y);
        rectanglePath2.lineTo(tanglePoint5.x, tanglePoint5.y);
        rectanglePath2.lineTo(tanglePoint6.x, tanglePoint6.y);
        rectanglePath2.close();
        /*
        * 设置内接圆三个圆的数据
        * */
        outSideCircleRectF.set(-outSideRadiu + circleCenterPoint.x, -outSideRadiu + circleCenterPoint.y, outSideRadiu + circleCenterPoint.x, outSideRadiu + circleCenterPoint.y);
        innerCircleRectF.set(-innerRadiu + circleCenterPoint.x, -innerRadiu + circleCenterPoint.y, innerRadiu + circleCenterPoint.x, innerRadiu + circleCenterPoint.y);
        innerInnerCircleRectF.set(-innerInnerRadiu + circleCenterPoint.x, -innerInnerRadiu + circleCenterPoint.y, innerInnerRadiu + circleCenterPoint.x, innerInnerRadiu + circleCenterPoint.y);
        //设置透明度
        bcPaint.setAlpha(outSideAlph);
        innerCirclePaint.setAlpha(innerAlph);
        Log.e("innerAlph", "initArcData: " + innerAlph );
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (bcScaleAnimatorSet != null && bcRotateAnimatorSet != null && bcAlphAnimatorSet != null && innerRotateAnimatorSet != null && innerScaleAnimatorSet != null ) {
            bcScaleAnimatorSet.start();
            bcRotateAnimatorSet.start();
            bcAlphAnimatorSet.start();
            innerRotateAnimatorSet.start();
            innerAlphAnimatorSet.start();
            innerAlphAnimatorSet.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (bcScaleAnimatorSet != null && bcRotateAnimatorSet != null && bcAlphAnimatorSet != null && innerRotateAnimatorSet != null && innerScaleAnimatorSet != null ) {
            bcScaleAnimatorSet.end();
            bcRotateAnimatorSet.end();
            bcAlphAnimatorSet.end();
            innerRotateAnimatorSet.end();
            innerAlphAnimatorSet.end();
            innerScaleAnimatorSet.end();
        }
    }


    private void draw() {
        try {
            synchronized (mSurfaceHolder) {
                canvas = mSurfaceHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    Log.e("TAG" , "outSideRadiu:" + outSideRadiu);
                    Log.e("TAG" , "outSideCircleStarAngle:" + outSideCircleStarAngle);
                    initArcData();
//                    drawFaceDetectRect();
                    drawArc(canvas);
                }
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) {
                //结束之后销毁这个view
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawFaceDetectRect() {
        for (int i = 0; i < faceSparseArray.size(); i++) {
            Face face = faceSparseArray.valueAt(i);
            canvas.drawRect(face.getPosition().x * xScale
                    , face.getPosition().y  * yScale
                    , face.getPosition().x + face.getWidth()  * xScale
                    , face.getPosition().y + face.getHeight() * yScale
                    , rectanglePaint);
        }
    }

    private void drawArc(Canvas canvas) {
        canvas.drawArc(outSideCircleRectF, outSideCircleStarAngle + outSideGapAngle, 180 - outSideGapAngle * 2, false, bcPaint);
        canvas.drawArc(outSideCircleRectF, outSideCircleStarAngle + 180 + outSideGapAngle, 180 - outSideGapAngle * 2, false, bcPaint);

        //内圆
        canvas.drawArc(innerCircleRectF, innerCircleStarAngle + innerGapAngle ,  120 - innerGapAngle * 2, false, innerCirclePaint);
        //内圆
        canvas.drawArc(innerCircleRectF, innerCircleStarAngle + 120 + innerGapAngle,  120 - innerGapAngle * 2, false, innerCirclePaint);
        //内圆
        canvas.drawArc(innerCircleRectF, innerCircleStarAngle + 240 + innerGapAngle,  120 - innerGapAngle * 2, false, innerCirclePaint);
        //最里面圆
        canvas.drawCircle(circleCenterPoint.x , circleCenterPoint.y , innerInnerRadiu , innerInnerCirclePaint);

        canvas.drawPoint(rectanglePoint1.x, rectanglePoint1.y, rectanglePaint);
        canvas.drawPoint(rectanglePoint2.x, rectanglePoint2.y, rectanglePaint);
        canvas.drawPath(rectanglePath1, rectanglePaint);
        canvas.drawPath(rectanglePath2, rectanglePaint);
        Log.e("drawArc", "drawArc: " + outSideCircleStarAngle);
    }

    /*
     * 根据三角形角度以及圆心坐标计算出两个三角形中心坐标
     * */
    private Point caculateRectanglePosition(Point centerPoint, int angle, float circleRadiu) {
        Point point = new Point();
        point.x = (int) (centerPoint.x + Math.cos(angle * Math.PI / 180) * circleRadiu);
        //y坐标和我们平时的坐标反过来
        point.y = (int) (centerPoint.y + Math.sin(angle * Math.PI / 180) * circleRadiu);
        return point;
    }



    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {

    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }
}
