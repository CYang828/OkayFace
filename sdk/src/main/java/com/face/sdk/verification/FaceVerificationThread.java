package com.face.sdk.verification;


import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.face.sdk.api.FaceInterfaceActivity;
import com.face.sdk.meta.DetectedFaces;
import com.face.sdk.meta.Face;
import com.face.sdk.meta.FaceFeature;
import com.face.sdk.meta.Person;
import com.face.sdk.repository.PersonRepository;
import com.face.sdk.scaffold.BridgeRunnable;
import com.face.sdk.utils.ImageUtils;

import java.util.ArrayList;


public class FaceVerificationThread<T extends FaceInterfaceActivity>  extends BridgeRunnable<DetectedFaces, ArrayList<Person>, T> {

    static {
        // 加载opencv3库
        System.loadLibrary("opencv_java3");
    }

    private static String TAG = FaceVerificationThread.class.getSimpleName();
    private static double THRESHOLD = 100;

    // 人脸编码模型
    private Facenet embedModel;

    // 人脸仓库
    PersonRepository personRepository;

    public FaceVerificationThread (Handler handler, T ctx) {
        super(handler, ctx);
        personRepository = new PersonRepository();
        embedModel = new Facenet(ctx);
    }

    @Override
    public void run() {
        while (!isFinished) {
            DetectedFaces detectedFaces = null;

            try {
                detectedFaces = inBridge.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long t_start = System.currentTimeMillis();

            for (final Face face: detectedFaces.getDetectFaces()) {
                // 切割人脸部分图像
                final Bitmap cropFaceBitmap = ImageUtils.cropFace(face, detectedFaces.getOriginalBitmap(), 0);
                face.setFaceBitmap(cropFaceBitmap);

                // 验证人脸图像模糊程度
                if (!ImageUtils.isBlurByOpenCV(cropFaceBitmap)) {
                    // 使用探测模型获取探测后的数据
                    FaceFeature faceFeature = embedModel.embeddingFaces(cropFaceBitmap);
                    face.setFaceFeature(faceFeature);

                    // 判断是否为新的面孔，分别通知
                    int personId = personRepository.isInRepo(face);


                    if (personId == -1) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ctx.verificationNewFaceResultCallback(face);
                            }
                        });
                    } else {
                        final Person person = personRepository.getFromRepo(personId);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ctx.verificationOldFaceResultCallback(person);

                            }
                        });
                    }
                }
                else {
                    Log.d(TAG, "face is flurry");
                }
            }

            lastCostTime = System.currentTimeMillis() - t_start;
            Log.d(TAG, "verify face cost time: " + Double.toString(lastCostTime));

            detectedFaces.clear();
        }
    }

    public double getFps() {
        return 1000 / lastCostTime;
    }

    public PersonRepository getPersonRepository() {
        return personRepository;
    }
}
