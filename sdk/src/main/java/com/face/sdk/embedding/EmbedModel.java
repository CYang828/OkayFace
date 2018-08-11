package com.face.sdk.embedding;


import android.graphics.Bitmap;

import com.face.sdk.meta.FaceFeature;


public interface EmbedModel {
    FaceFeature embeddingFaces(Bitmap bitmap);
}
