package com.skyworth.easysocket;


import android.util.Log;

import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.HeartMessage;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 作者：Ice Nation
 * 日期：2018/5/11 16:47
 * 邮箱：tangjie@skyworth.com
 */

public class ReceiveThread extends Thread {

    protected final String TAG = getClass().getSimpleName();

    private static final int MAX_READ_LEN = 10*1024;

    private static final int MIN_READ_LEN = 8;

    private DataInputStream dataInputStream = null;

    private boolean isRunning = true;

    private OnReceiveListener mReceiveListener = null;


    public ReceiveThread(DataInputStream dataInputStream) {
        this("ReceiveThread",dataInputStream);
    }

    public ReceiveThread(String name,DataInputStream dataInputStream){
        super(name);
        this.dataInputStream = dataInputStream;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }


    /**
     * 从数组的<code>start</code>位置拷贝长度为<code>len</code>的byte数组
     * @param start 复制起点
     * @param len 复制长度
     * @return 复制得到的数组
     */
    public byte[] readBytes(byte[] bytes,int start, int len) throws Exception{
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        if(len <= 0){
            throw new IndexOutOfBoundsException("read len <= 0");
        }
        byte[] tempBytes = new byte[len];
        System.arraycopy(bytes, start, tempBytes, 0, len);
        return tempBytes;
    }

    /**
     * 从数组的<code>start</code>位置起读取一个<type>int</type>型数据
     * @param start 读取起点
     * @return 读取的数据
     */
    public int readInt(byte[] buffer,int start) {
        byte[] bytes = new byte[0];
        try {
            bytes = readBytes(buffer,start,4);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        int value = 0;
        for (int i = 0; i < 4 ; i++) {
            value |= (bytes[i] & 0xFF) << ((4 - i - 1) * 8 ) ;
        }
        return value;
    }

    @Override
    public void run() {
        super.run();
        //每次最多读取10KB
        byte[] readBuffer = new byte[MAX_READ_LEN];
        byte[] cacheBuffer = null;
        byte[] endBuffer = new byte[0];
        int cacheOffset = 0;

        int head = 0;
        int packLen = 0;
        int unReadLen = 0;

        boolean hasHead = false;
        while (isRunning) {
            try {
                int readLen = 0;
                //如果有上次未处理的数据，添加到readBuffer头部
                int endLen = endBuffer.length;
                if(endLen > 0){
                    System.arraycopy(endBuffer,0,readBuffer,0,endLen);
                    readLen = dataInputStream.read(readBuffer,endLen, MAX_READ_LEN - endLen);
                    readLen += endLen;
                }else readLen = dataInputStream.read(readBuffer);

//                byte[] bytes = new byte[readLen];
//                System.arraycopy(readBuffer,0,bytes,0,readLen);
//                Log.i(TAG, Arrays.toString(bytes));

                int offset = 0;
                while (true){
                    if(hasHead){
                        //有head时说明当前buffer一开始紧接上一个buffer最后一个head的内容
                        //判断未读取数据长度是否大于buffer长度
                        if(unReadLen <= readLen){
                            //小于buffer长度，说明上一个head在该buffer中结尾
                            System.arraycopy(readBuffer,0,cacheBuffer,cacheOffset,unReadLen);
                            //保存并通过message广播出去
                            onReceive(cacheBuffer,0,cacheBuffer.length);
                            //当前偏移 + unReadLen
                            offset += unReadLen;
                            unReadLen = 0;
                            hasHead = false;
                        }else {
                            //大于buffer长度，说明该buffer中所有数据均为上一个head的内容
                            //全部保存
                            System.arraycopy(readBuffer,0,cacheBuffer,cacheOffset,readLen);
                            cacheOffset += readLen;
                            //刷新未读取的长度
                            unReadLen -= readLen;
                            break;
                        }
                    }else {

                        //判断当前可读空间是否可读head
                        int canReadLen = readLen - offset;
                        if(canReadLen < 0)
                            continue;
                        if(canReadLen < 8){
                            endBuffer = new byte[canReadLen];
                            System.arraycopy(readBuffer,offset,endBuffer,0,canReadLen);
                            break;
                        }

                        //从当前偏移位置处读取数据包头部
                        head = readInt(readBuffer,offset);
                        packLen = readInt(readBuffer, offset + 4);
                        endBuffer = new byte[0];
                        //读取请求码
                        //int request = readInt(buffer,8);
                        //Log.i(TAG,"request = " + request);
                        //则判断是否合格
                        if(!isTrue(head,packLen)) {
                            //不合格，跳过4个字节，继续读head
                            offset += 4;
                            continue;
                        }
                        offset += 8;
                        //判断数据包某尾是否在有效长度内
                        if(packLen + offset <= readLen){
                            //存在一包数据，通过message广播出去
                            onReceive(readBuffer,offset,packLen);
                            //当前读取数据的索引位置 + packLen
                            offset += packLen;
                        }else {
                            //说明其余数据都是当前数据包内容
                            hasHead = true;
                            //新建一个能保存当前数据包所有内容的缓冲区
                            cacheBuffer = new byte[packLen];
                            //将当前buffer的这部分保存下来
                            cacheOffset = readLen - offset;
                            System.arraycopy(readBuffer,offset,cacheBuffer,0,cacheOffset);
                            //设置未保存的长度
                            unReadLen = packLen - cacheOffset;
                            break;
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG,"receive error::" + e.getMessage());
                isRunning = false;
                if(mReceiveListener != null)
                    mReceiveListener.onError(this,e);
                try {
                    if(dataInputStream != null)
                        dataInputStream.close();
                } catch (IOException e2) {
                    Log.i(TAG,"receive close failed!");
                    return;
                }
            }
        }
        if(mReceiveListener != null)
            mReceiveListener.onStopped(this);
        Log.i(TAG,"receive thread stopped!");
    }

    private boolean isTrue(int head, int packLen) {
        return  Protocol.HEAD == head
                && packLen >= 4;
    }

    protected void onReceive(byte[] bytes, int offset, int len){
        byte[] data = new byte[len];
        System.arraycopy(bytes,offset,data,0,len);
        EasyMessage message = new EasyMessage(data,len);
        EasyMessage msg = message;
        switch (message.type){
            case Protocol.HEART:
                if(message.code == Protocol.HEART_ASK){
                    msg = new HeartMessage(message);
                }
                break;
        }
        onReceive(msg);
    }

    protected void onReceive(EasyMessage message) {
        if(mReceiveListener!=null)
            mReceiveListener.onReceive(this,message);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setInputStream(InputStream inputStream) {
        this.dataInputStream = new DataInputStream(inputStream);
    }

    public interface OnReceiveListener{

        /**
         * 收到服务端数据时调用
         * @param message
         */
        void onReceive(Thread thread,EasyMessage message);

        void onError(Thread thread,Exception e);

        void onStopped(Thread thread);
    }

    public void setOnReceiveListener(OnReceiveListener listener) {
        this.mReceiveListener = listener;
    }
}
