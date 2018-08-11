package com.face.sdk.verification;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.face.sdk.R;
import com.face.sdk.embedding.EmbedModel;
import com.face.sdk.meta.FaceFeature;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.InputStream;


/**
 * 功能：人脸转换为512维特征向量
 */
public class Facenet implements EmbedModel {

    private static final String TAG = Facenet.class.getSimpleName();

    // 模型资源
    private static final int MODEL_IDENTIFIER = R.raw.facenet_20180726;

    // 模型各层名称
    private static final String INPUT_NAME = "input:0";
    private static final String OUTPUT_NAME = "embeddings:0";
    private static final String PHASE_NAME = "phase_train:0";
    private static final String[] outputNames = new String[]{OUTPUT_NAME};

    // 神经网络输入大小
    private static final int INPUT_SIZE = 160;
    // 保存input的值
    private float[] floatValues;
    // 像素值
    private int[] intValues;
    private TensorFlowInferenceInterface inferenceInterface;
    public double fps = 0;
    private Context ctx;
    private double lastCostTime;

    public Facenet(Context ctx) {
        this.ctx = ctx;
        loadModel();
        lastCostTime = 0;
        floatValues = new float[INPUT_SIZE * INPUT_SIZE * 3];
        intValues = new int[INPUT_SIZE * INPUT_SIZE];
    }

    /**
     * 加载模型
     */
    private boolean loadModel() {
        InputStream in = ctx.getResources().openRawResource(MODEL_IDENTIFIER);

        try {
            inferenceInterface = new TensorFlowInferenceInterface(in);
            Log.d(TAG, "facenet_20180726 load model success");
        } catch (Exception e) {
            Log.e(TAG, "facenet_20180726 load model failed" + e);
            return false;
        }
        return true;
    }

    /**
     * 位图转换浮点类型
     */
    private int normalizeImage(final Bitmap _bitmap) {
        // (0) bitmap缩放到INPUT_SIZE*INPUT_SIZE
        float scale_width = ((float) INPUT_SIZE) / _bitmap.getWidth();
        float scale_height = ((float) INPUT_SIZE) / _bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_width, scale_height);
        Bitmap bitmap = Bitmap.createBitmap(_bitmap, 0, 0, _bitmap.getWidth(), _bitmap.getHeight(), matrix, true);
        //Log.d("Facenet","[*]bitmap size:"+bitmap.getHeight()+"x"+bitmap.getWidth());
        // (1) 将像素映射到[-1,1]区间内
        float imageMean = 127.5f;
        float imageStd = 128;
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; i++) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
        }
        //Log.d("Facenet","[*]normalizeImage");
        //Log.d("Facenet","[*]normalizeImage"+intValues.length);
        return 0;
    }

    @Override
    public FaceFeature embeddingFaces(Bitmap bitmap) {
        long t_start = System.currentTimeMillis();
        //Log.d("Facenet","[*]recognizeImage");
        //(0)图片预处理，normailize
        normalizeImage(bitmap);
        //(1)Feed
        try {
            inferenceInterface.feed(INPUT_NAME, floatValues, 1, INPUT_SIZE, INPUT_SIZE, 3);
            boolean[] phase = new boolean[1];
            phase[0] = false;
            inferenceInterface.feed(PHASE_NAME, phase);
        } catch (Exception e) {
            Log.e(TAG, "facenet_20180726 feed Error\n" + e);
            return null;
        }
        //(2)run
        // Log.d("Facenet","[*]Feed:"+INPUT_NAME);
        try {
            inferenceInterface.run(outputNames, false);
        } catch (Exception e) {
            Log.e("Facenet", "facenet_20180726 run error\n" + e);
            return null;
        }
        //(3)fetch
        FaceFeature faceFeature = new FaceFeature();
        float[] outputs = faceFeature.getFeature();
        try {
            inferenceInterface.fetch(OUTPUT_NAME, outputs);
        } catch (Exception e) {
            Log.e("Facenet", "facenet_20180726 fetch error\n" + e);
            return null;
        }

        lastCostTime = System.currentTimeMillis() - t_start;
        return faceFeature;
    }

    public double getLastCostTime() {
        return lastCostTime;
    }
}