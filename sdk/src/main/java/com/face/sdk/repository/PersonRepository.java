package com.face.sdk.repository;

import android.util.Log;

import com.face.sdk.meta.Face;
import com.face.sdk.meta.Person;

import java.util.ArrayList;

public class PersonRepository {

    private static double SIMILARITY_THREADHOLD = 1.1;

    private ArrayList<Person> personRepo;

    public PersonRepository() {
        personRepo = new ArrayList();
    }

    /**
     * 此人是否在仓库中
     *
     * 返回list索引，-1则为不存在
     */
    public int isInRepo(Face face) {
        int ret = -1;
        double dist = 0;

        for(int i=0; i < personRepo.size(); i++) {
            dist = personRepo.get(i).getFace().getFaceFeature().compare(face.getFaceFeature());
            Log.d("similarity", Double.toString(dist));
            if (dist > SIMILARITY_THREADHOLD) {
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
