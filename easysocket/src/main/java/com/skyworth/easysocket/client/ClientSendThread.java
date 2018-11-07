package com.skyworth.easysocket.client;


import com.skyworth.easysocket.SendThread;
import com.skyworth.easysocket.bean.EasyMessage;

import java.io.OutputStream;

/**
 * 作者：Ice Nation
 * 日期：2018/4/11 18:00
 * 邮箱：tangjie@skyworth.com
 */

public class ClientSendThread extends SendThread implements HeartProducer.OnHeartListener{


    //心跳机
    private HeartProducer mHeartProducer = null;

    public void setHeartPause() {
        mHeartProducer.pause();
    }

    public void setHeartProceed() {
        mHeartProducer.proceed();
    }

    public ClientSendThread(OutputStream outputStream){
        super(outputStream);
        mHeartProducer = new HeartProducer();
        mHeartProducer.setOnHeartListener(this);
    }

    @Override
    public void run() {
        super.run();
        //停止心跳产生
        mHeartProducer.stop();
    }

    @Override
    public synchronized void start() {
        super.start();
        //心跳机和发送线程同时运行
        mHeartProducer.start();
    }

    @Override
    public void onHeartProduce(EasyMessage message) {
        putPack(message);
    }

}
