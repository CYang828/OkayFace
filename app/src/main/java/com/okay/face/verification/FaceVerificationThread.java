//package com.okay.face.verification;
//
//
//import android.graphics.Bitmap;
//import android.os.Handler;
//import android.util.Log;
//
//import com.okay.face.activity.OkayFaceActivity;
//
//import java.util.Iterator;
//import java.util.Vector;
//
//
//public class FaceVerificationThread<T extends OkayFaceActivity>  implements Runnable {
//
//    private static String TAG = FaceVerificationThread.class.getSimpleName();
//
//    private Facenet facenet;
//    private Vector<Bitmap> facesBitmap;
//
//    public FaceVerificationThread (Handler handler, T ctx) {
//        facenet = new Facenet(ctx.getAssets());
//    }
//
//    @Override
//    public void run() {
//        Iterator<Bitmap> iter = facesBitmap.iterator();
//        while (iter.hasNext()) {
//            Bitmap faceBitmap = iter.next();
//            FaceFeature faceFeature = facenet.recognizeImage(faceBitmap);
//
//        }
//    }
//
//    public void setFaces(Vector<Bitmap> facesBitmap) {
//        this.facesBitmap = facesBitmap;
//    }
//}
