package com.skyworth.easysocket;


import android.util.Log;

import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.server.ServerSendThread;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * 作者：Ice Nation
 * 日期：2018/5/11 17:05
 * 邮箱：tangjie@skyworth.com
 */

public class SendThread extends Thread {

    public final String TAG = getClass().getSimpleName();

    //Socket输出流
    private OutputStream outputStream = null;

    //要发送的数据
    private List<EasyMessage> dataList = null;
    //正在发送的数据
    private List<EasyMessage> cacheList = null;
    //运行状态
    private boolean isRunning = true;
    //是否暂停
    private boolean paused = false;
    //错误事件
    private ServerSendThread.OnErrorListener listener = null;

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public interface OnErrorListener{
        /**
         * 当发送消息产生错误和异常时调用
         * @param e 产生的异常
         */
        void onError(Exception e);
    }

    public void setOnErrorListener(ServerSendThread.OnErrorListener listener) {
        this.listener = listener;
    }

    public SendThread(){
        this(null);
    }

    public SendThread(OutputStream outputStream){
        this.outputStream = outputStream;
        dataList = new LinkedList<>();
        cacheList = new LinkedList<>();
    }

    public synchronized void keepRunning(){
        if(paused){
            synchronized (this){
                notify();
            }
        }
    }

    public synchronized void shutdown(){
        isRunning = false;
        keepRunning();
    }

    @Override
    public void run() {
        super.run();
        while (isRunning){
            //如果没有暂停
            if(!dataList.isEmpty()){
                /**
                 * 发送数据时为了保证对List长度操作的线程安全性，先将原dataList数据拷贝到
                 * 缓冲并清空cacheList，此时原dataList可以被操作而不会影响缓冲并清空cacheList，
                 * 将缓冲并清空cacheList数据发送完毕后，再对应移除原dataList中包含的缓冲并清空
                 * cacheList元素，并清空cacheList。
                 */
                //同步拷贝集合内容到缓冲区
                synchronized (this){
                    cacheList.addAll(dataList);
                }
                for (EasyMessage message : cacheList) {
                    onSendData(message);
                }
                //同步移除集合内缓冲区内容
                synchronized (this){
                    dataList.removeAll(cacheList);
                    cacheList.clear();//清除缓冲区
                }
            }
            else {
                synchronized (this){
                    try {
                        paused = true;
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    paused = false;
                }
            }
        }
        //如果还有数据没发完
        if(dataList.size()>0){
            for (EasyMessage message : cacheList) {
                onSendData(message);
            }
        }
        Log.i(TAG,"Send Thread stopped!");
    }

    /**
     * 添加一个已封装数据包
     * @param message 已封装数据包
     */
    public synchronized void putPack(EasyMessage message){
        dataList.add(message);
        keepRunning();
    }


    protected synchronized void onSendData(EasyMessage msg){
        send(msg.getBytes());
    }

    /**
     * 使用输出流发送数据，产生异常后调用{@link ServerSendThread.OnErrorListener}
     * 使受监听的对象作出处理
     * @param data 要发送的数据
     */
    public void send(final byte[] data) {
        if(outputStream == null){
            Log.i(TAG,"outputStream = null,send fail!");
            return;
        }
        synchronized(this) {
            try {
                outputStream.write(Utils.getBytes(Protocol.HEAD));
                outputStream.write(Utils.getBytes(data.length));
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException e) {
                Log.i(TAG,"send error::"+e.getMessage());
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e1) {
                    Log.i(TAG,"close error::"+e1.getMessage());
                }
                //上报流错误
                if(listener!=null){
                    listener.onError(e);
                }
            }
        }
    }
}
