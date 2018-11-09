package com.skyworth.easysocket.client;


import android.support.annotation.NonNull;
import android.util.Log;

import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.ReceiveThread;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 作者：Ice Nation
 * 日期：2018/4/10 19:24
 * 邮箱：tangjie@skyworth.com
 */

public class TCPClient implements ClientSender,ClientSendThread.OnErrorListener {
    public static final String TAG = "TCPClient";

    private String serverIp = null;
    private int serverPort = 0;
    private boolean isConnected = false;
    //自动重连开关
    private boolean isAutoConnect = false;
    //自动重连次数
    private int autoConnectCount = 3;

    private Socket socket = null;
    private DataInputStream dataInputStream = null;
    private OutputStream outputStream = null;

    private ClientSendThread mSendThread = null;
    private ReceiveThread mReceiveThread = null;

    private OnConnectedListener mConnectedListener = null;
    private ReceiveThread.OnReceiveListener mClientReceiver = null;

    @Override
    public void onError(final Exception e ) {
        Log.i(TAG,"正在尝试重新连接...");
        reconnectTCP();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getLocalPort() {
        return socket != null && socket.isConnected() ? socket.getLocalPort():0;
    }

    public interface OnConnectedListener{
        void onConnected(int port, String ip);

        void onConnectFail(Exception e);

        void onDisconnected(int port, String ip);

        void onReconnected(int port, String ip);
    }

    public void setOnConnectedListener(OnConnectedListener mConnectedListener) {
        this.mConnectedListener = mConnectedListener;
    }

    public TCPClient(){
    }

    public TCPClient(SocketInfo info){
        setServerInfo(info);
    }

    public void setServerInfo(SocketInfo info){
        serverIp = info.getIp();
        serverPort = info.getPort();
    }

    public void setServerInfo(String ip,int port){
        serverIp = ip;
        serverPort = port;
    }

    public void setOnReceiveListener(ReceiveThread.OnReceiveListener receiverListener) {
        this.mClientReceiver = receiverListener;
    }

    public void connect(){
        isAutoConnect = true;
        autoConnectCount = 3;
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectTCP();
            }
        }).start();
    }

    public void disconnect(){
        isAutoConnect = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectTCP();
            }
        }).start();
    }

    public void reconnectTCP(){
        autoConnectCount -- ;
        //在其他线程中处理异常
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isAutoConnect){
                    Log.i(TAG,"正在重新连接...");
                    disconnectTCP();
                    connectTCP();
                    if(mConnectedListener != null){
                        mConnectedListener.onReconnected(serverPort,serverIp);
                    }

                }
            }
        }).start();
    }

    public void connectTCP(){
        //停止发送和接收
        if(isConnected){
            Log.i(TAG,"already has connected");
            return;
        }
        Log.i(TAG,"connecting ...");
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(serverIp, serverPort), 5000);
            dataInputStream = new DataInputStream(socket.getInputStream());
            outputStream = socket.getOutputStream();

            socket.setPerformancePreferences(0, 2, 1);
            socket.setTcpNoDelay(true);
            socket.setReceiveBufferSize(1024 * 1024);
            socket.setSendBufferSize(1024 * 1024);
        }
        catch (Exception e){
            Log.i(TAG,"Connect error::" + e.getMessage());

            if(autoConnectCount <= 0) {
                isAutoConnect = false;
                if(mConnectedListener!=null)
                    mConnectedListener.onConnectFail(new Exception("server offline"));
            }else {
                if(mConnectedListener!=null)
                    mConnectedListener.onConnectFail(e);
                reconnectTCP();
            }
            return;
        }

        mSendThread = new ClientSendThread(outputStream);

        mReceiveThread = new ReceiveThread(dataInputStream);
        mReceiveThread.setRunning(true);

        int timeCount = 0;
        //等待连接成功
        while (true){
            if(socket.isConnected()) {
                Log.i(TAG,"connect success!");
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeCount += 50;
            if(timeCount >= 3000){
                Log.i(TAG,"connect failed!");
                return;
            }
        }
        //连接成功
        isConnected = true;
        //设置接收线程的接收对象
        mReceiveThread.setOnReceiveListener(mClientReceiver);
        mSendThread.setOnErrorListener(TCPClient.this);
        //广播发送连接的消息
        if(mConnectedListener !=null){
            mConnectedListener.onConnected(serverPort,serverIp);
        }

        //开启发送和接收线程
        mSendThread.start();
        mReceiveThread.start();

    }



    public void disconnectTCP(){
        //如果当前有连接 开始断开程序
        if(!isConnected)
            return;
        //发送即将断开连接的广播
        if(mConnectedListener != null)
            mConnectedListener.onDisconnected(serverPort,serverIp);
        //停止发送和接收线程
        mSendThread.putPack(EasyMessage.create(Protocol.DISCONNECT,0));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mSendThread.shutdown();
        mReceiveThread.setRunning(false);
        //停止socket连接
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSendThread = null;
        mReceiveThread = null;
        isConnected = false;
        System.gc();
    }

    private void close() throws IOException {
        this.socket.shutdownInput();
        this.socket.shutdownOutput();
        this.dataInputStream.close();
        this.outputStream.close();
        this.socket.close();
    }

    @Override
    public void send(@NonNull EasyMessage message) {
        if(mSendThread!= null)
            mSendThread.putPack(message);
    }
}