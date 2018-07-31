package com.okay.face.detection;


import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.okay.face.activity.OkayFaceActivity;
import com.okay.face.verification.FaceVerificationDispatchThread;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 人脸探测线程
 */
public class FaceDetectThread<T extends OkayFaceActivity> implements Runnable {

    private static String TAG = FaceDetectThread.class.getSimpleName();
    private static int MINI_FACE_SIZE = 100;

    private MTCNN detect_model;
    private Vector<Face> faces;
    private Bitmap bitmap;
    private boolean finished = false;
    private LinkedBlockingQueue<Bitmap> preprocessdeBitmapBridge;
    private LinkedBlockingQueue<FaceDetectResult > faceDetectBridge;

    private T ctx;
    private float scaleX;
    private float scaleY;
    private Handler handler;
    private double costTime = 0;

    public FaceDetectThread(Handler handler, T ctx) {
        this.ctx = ctx;
        this.handler = handler;
        detect_model = new MTCNN(ctx.getAssets());
        faceDetectBridge = new LinkedBlockingQueue();
    }

    @Override
    public void run() {
        while (!finished) {
            long t_start = System.currentTimeMillis();

            try {
                this.bitmap = this.preprocessdeBitmapBridge.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (this.bitmap != null) {
                faces = detect_model.detectFaces(this.bitmap, MINI_FACE_SIZE);

                Iterator<Face> iter = faces.iterator();
                while (iter.hasNext()) {
                    Face face = iter.next();
                    face.transform2OriginalBox(this.scaleX, this.scaleY);
                }

                // 通知渲染人脸框
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ctx.paintDetctionResult(faces, detect_model.fps);
                    }
                });

                if (!FaceVerificationDispatchThread.isIsVerifying()) {
                    try {
                        FaceDetectResult faceDetectResult = new FaceDetectResult(bitmap, faces);
                        faceDetectBridge.put(faceDetectResult);
                        Log.i(TAG, Integer.toString(this.faceDetectBridge.size()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                this.costTime = System.currentTimeMillis() - t_start;
                Log.i(TAG, "Detect Face Cost Time: " + Double.toString(this.costTime));
            }
        }
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public void setPreprocessdeBitmapBridge(LinkedBlockingQueue<Bitmap> preprocessdeBitmapBridge) {
        this.preprocessdeBitmapBridge = preprocessdeBitmapBridge;
    }

    public LinkedBlockingQueue<FaceDetectResult > getFaceDetectBitmapBridge() {
        return this.faceDetectBridge;
    }

}
