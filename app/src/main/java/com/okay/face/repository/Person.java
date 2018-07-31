package com.okay.face.repository;


import android.graphics.Bitmap;

import com.okay.face.detection.Face;
import com.okay.face.verification.FaceFeature;


public class Person {
    private int id;
    private String name;
    private Bitmap faceBitmap;
    private Face face;

    public Person(String name, Bitmap faceBitmap, Face face) {
        this.name = name;
        this.faceBitmap = faceBitmap;
        this.face = face;
    }

    public Face getFace() {
        return face;
    }

    public Bitmap getFaceBitmap() {
        return faceBitmap;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
