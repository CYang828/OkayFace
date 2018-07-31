package com.okay.face.repository;

import android.util.Log;

import com.okay.face.verification.FaceFeature;

import java.util.ArrayList;

public class PersonRepository {

    private static double SIMILARITY_THREADHOLD = 1.2;

    private ArrayList<Person> personRepo;

    public PersonRepository() {
        personRepo = new ArrayList();
    }

    /**
     * 此人是否在仓库中
     *
     * 返回list索引，-1则为不存在
     */
    public int isInRepo(FaceFeature faceFeature) {
        int ret = -1;

        for(int i=0; i < personRepo.size(); i++) {
            if (personRepo.get(i).getFace().getFaceFeature().compare(faceFeature) > SIMILARITY_THREADHOLD) {
                ret = -1;
            }
            else
            {
                // 这里的逻辑需要优化
                ret = i;
                break;
            }
        }

        return ret;
    }

    /**
     * 放入仓库
     */
    public void putInRepo(Person person) {
        personRepo.add(person);
        Log.i("put in repo", Integer.toString(personRepo.size()));
    }

    /**
     * 如果此人在仓库中，找到并返回
     */
    public Person getFromRepo(int index) {
        return personRepo.get(index);
    }

}
