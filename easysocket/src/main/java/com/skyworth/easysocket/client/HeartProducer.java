package com.skyworth.easysocket.client;


import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.HeartMessage;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 作者：Ice Nation
 * 日期：2018/4/12 09:11
 * 邮箱：tangjie@skyworth.com
 */

public class HeartProducer {

    public static final String TAG = "HeartProducer";
    //心跳包内容
    private String mHeartData = Protocol.HEART_TAG;
    //心跳频率
    private int mFrequency = 3000;

    private Timer mTimer = null;
    //计时器中断时间
    private int mPeriodTime = 100;
    //当前计时时间
    private int mTimeCount = 0;
    //计时器暂停/继续
    private boolean isRunning = true;
    //产生心跳事件
    private OnHeartListener mHeartListener = null;

    private EasyMessage message = null;

    interface OnHeartListener{
        /**
         * 产生心跳的事件，
         * @param converter 心跳内容的封装
         */
        void onHeartProduce(EasyMessage converter);
    }

    public void setOnHeartListener(OnHeartListener mHeartListener) {
        this.mHeartListener = mHeartListener;
    }

    public HeartProducer(){

    }

    /**
     * 设置心跳内容和频率
     * @param data 心跳包字符内容
     * @param freq 心跳产生频率(ms/次)
     */
    public HeartProducer(String data,int freq){
        this.mHeartData = data;
        this.mFrequency = freq;
    }

    public void setHeartData(String mHeartData) {
        this.mHeartData = mHeartData;
    }

    public void setFrequency(int mFrequency) {
        this.mFrequency = mFrequency;
    }


    //暂停心跳产生
    public void pause() {
        isRunning = false;
    }

    //恢复心跳产生
    public void proceed() {
        isRunning = true;
    }

    /**
     * 开始产生心跳
     */
    public void start(){
        mTimer = new Timer(TAG);
        mTimer.schedule(new HeartTimerTask(),100,mPeriodTime);
        message = HeartMessage.create(mHeartData,Protocol.HEART_ASK);
        if(mHeartListener != null){
            mHeartListener.onHeartProduce(message);
        }
    }

    /**
     * 停止产生心跳，并释放计时器资源
     */
    public void stop(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    class HeartTimerTask extends TimerTask {

        @Override
        public void run() {
            //暂停时计数器归零
            if(!isRunning){
                mTimeCount = 0;
                return;
            }
            mTimeCount+=mPeriodTime;
            if(mTimeCount >= mFrequency){
                if(mHeartListener != null){
                    mHeartListener.onHeartProduce(message);
                    mTimeCount = 0;
                }
            }
        }
    }

}
