package com.okay.face.verification;


import com.okay.face.MainActivity;
import com.okay.face.activity.OkayFaceActivity;
import com.okay.face.detection.Face;
import com.okay.face.detection.FaceDetectResult;
import com.okay.face.repository.Person;
import com.okay.face.utils.ImageUtils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


public class FaceVerificationDispatchThread<T extends OkayFaceActivity> implements Runnable {

    private static String TAG = FaceVerificationDispatchThread.class.getSimpleName();
    private static boolean isVerifying = false;

    private T ctx;
    private Handler handler;
    Facenet facenet;
    private boolean finished = false;
    private FaceDetectResult detectFaceResult;
    private LinkedBlockingQueue<FaceDetectResult> faceDetectionBridge;
    private ThreadPoolExecutor faceVerifiationExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public FaceVerificationDispatchThread (Handler handler, T ctx) {
        this.handler = handler;
        this.ctx = ctx;
        facenet = new Facenet(ctx.getAssets());
        faceVerifiationExecutor.setCorePoolSize(5);
        faceVerifiationExecutor.setMaximumPoolSize(5);
    }

    public static boolean isIsVerifying() {
        return isVerifying;
    }

    @Override
    public void run() {
        while (!finished) {
            try {
                Log.i(TAG, Integer.toString(this.faceDetectionBridge.size()));
                detectFaceResult = this.faceDetectionBridge.take();
                isVerifying = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 将识别到的人脸切图
            Bitmap originalBitmap = detectFaceResult.getOriginalBitmap();
            Vector<Face> detectFaces = detectFaceResult.getDetectFaces();

            // 只有当画面中有且仅有1张脸时才进行脸部识别工作
            if (detectFaces.size() == 1) {
                Iterator<Face> iter = detectFaces.iterator();
                while (iter.hasNext()) {
                    final Face face = iter.next();
                    final Bitmap cropFaceBitmap = ImageUtils.cropFace(face, originalBitmap, 0);
                    Log.i(TAG, Integer.toString(cropFaceBitmap.getWidth()) + "," + Integer.toString(cropFaceBitmap.getHeight()));
                    FaceFeature faceFeature = facenet.recognizeImage(cropFaceBitmap);
                    face.setFaceFeature(faceFeature);

                    int persionIndex = MainActivity.personRepository.isInRepo(faceFeature);
                    if (persionIndex == -1) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ctx.popNewUserActivity(cropFaceBitmap, face);
                            }
                        });
                    }
                    else {
                        final Person person = MainActivity.personRepository.getFromRepo(persionIndex);
                        Log.i(TAG, person.getName());

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ctx.paintVerifyResult(person, facenet.fps);
                            }
                        });
                    }

                }
            }



            // 脸部编码和比对的工作交给子线程来完成
            /*
            if (detectFaces.size() > 0) {
                Thread realFaceVerificationThread = new FaceVerificationThread(handler, ctx).setFaces(detectFaces);
                faceVerifiationExecutor.execute(realFaceVerificationThread);
            }
            */
            isVerifying = false;
        }
    }

    public void setFaceDetectionBridge(LinkedBlockingQueue<FaceDetectResult> faceDetectionBitmapBridge) {
        this.faceDetectionBridge = faceDetectionBitmapBridge;
    }
}
