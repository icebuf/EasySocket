package com.skyworth.easysocket.server;


import com.skyworth.easysocket.SendThread;
import com.skyworth.easysocket.bean.EasyMessage;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者：Ice Nation
 * 日期：2018/4/11 18:00
 * 邮箱：tangjie@skyworth.com
 */

public class ServerSendThread extends SendThread {

    private Map<EasyMessage,OutputStream> outputStreamMap = null;

    public ServerSendThread(){
        this(null);
    }

    public ServerSendThread(OutputStream outputStream){
        super(outputStream);
        outputStreamMap = new HashMap<>();
    }

    /**
     * 发送数据时为了保证对List长度操作的线程安全性，先将原dataList数据拷贝到
     * 缓冲并清空cacheList，此时原dataList可以被操作而不会影响缓冲并清空cacheList，
     * 将缓冲并清空cacheList数据发送完毕后，再对应移除原dataList中包含的缓冲并清空
     * cacheList元素，并清空cacheList。
     */
    @Override
    public synchronized void  onSendData(EasyMessage msg){
        setOutputStream(outputStreamMap.get(msg));;
        send(msg.getBytes());
        outputStreamMap.remove(msg);
    }

    /**
     * 添加一个已封装数据包
     * @param message 已封装数据包
     */
    public synchronized void putPack(EasyMessage message, OutputStream stream){
        putPack(message);
        outputStreamMap.put(message,stream);
    }
}
