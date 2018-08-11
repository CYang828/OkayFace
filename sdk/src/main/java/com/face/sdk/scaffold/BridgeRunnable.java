package com.face.sdk.scaffold;

import android.os.Handler;
import android.util.Log;

import com.face.sdk.api.FaceInterfaceActivity;

import java.util.concurrent.LinkedBlockingQueue;


public abstract class BridgeRunnable<IN, OUT, T extends FaceInterfaceActivity> implements Runnable {

    // 桥入口
    public LinkedBlockingQueue<IN> inBridge;
    // 桥出口
    public LinkedBlockingQueue<OUT> outBridge;
    // 桥出口端
    public BridgeRunnable outBridgeEnd;
    // 结束标志
    public boolean isFinished;
    // 上次执行花费时间
    public double lastCostTime;
    // 与UI线程通信句柄
    public Handler handler;
    // 调起activity类
    public T ctx;
    // 处理计数器
    public int counter;
    // 丢弃间隔
    public int interval;


    public BridgeRunnable(Handler handler, T ctx) {
        inBridge = new LinkedBlockingQueue();
        outBridge = null;
        isFinished = false;
        lastCostTime = 0;
        this.handler = handler;
        this.ctx = ctx;
        outBridgeEnd = null;
        counter = 0;
        interval = 1;
    }

    /**
     * 获取桥入口
     */
    public LinkedBlockingQueue<IN> getInBridge() {
        return inBridge;
    }

    /**
     * 设置桥出口端
     */
    public void setOutBridgeEnd(BridgeRunnable outBridgeEnd) {
        this.outBridgeEnd = outBridgeEnd;
        this.outBridge = outBridgeEnd.inBridge;
    }

    /**
     * 设置过滤间隔
     */
    public int setInterval() {
        counter = 0;
        return 1;
    }

    /**
     * 从桥入口放入数据
     */
    public void pullInBridge(IN in) throws InterruptedException {
        counter++;
        int interval = setInterval();
        if ((inBridge.size() < 2) && (interval != 0) && (counter % interval == 0)) {
            inBridge.put(in);
            counter = 0;
        }
    }

    public int getInterval() {
        return interval;
    }

    /**
     * 结束线程
     */
    public void stop() {
        isFinished = true;
        inBridge.clear();
    }
}
