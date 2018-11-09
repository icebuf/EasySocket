package com.skyworth.easysocket.server;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.ReceiveThread;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;
import com.skyworth.easysocket.bean.SocketInfoMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 作者：Ice Nation
 * 日期：2018/5/8 10:30
 * 邮箱：tangjie@skyworth.com
 */

public class TCPServer implements ServerSender {

    private static final String TAG = "TCPServer";

    private static final int MAX_CONNECT_COUNT = 6;

    private ServerSocket mServerSocket = null;
    //服务端监听客户端连接的线程
    private ServerThread mServerThread = null;
    //服务端监听的端口
    private int mListenerPort;
    //连接的客户端列表
    private List<Socket> mClientList = null;
    //服务端接收各个客户端数据的处理线程池
    private ExecutorService mExecutorService = null;
    //服务端发送数据线程
    private ServerSendThread mSendThread = null;
    //收到客户端数据的监听
    private OnReceiveListener mReceiveListener = null;

    private ConnectionListener mConnectionListener = null;

    private Broadcaster mBroadcaster = null;

    private Handler mHandler;

    public TCPServer(){
        this(Protocol.TCP_PORT);
    }

    public TCPServer(int port){
        this(port,null);
    }

    public TCPServer(int port, Looper looper){
        setPort(port);
        if(looper == null)
            return;
        mHandler = new Handler(looper,new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 100:

                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    public void start(){
        if(mListenerPort == 0) {
            return;
        }

        mClientList = new ArrayList<>();
        mExecutorService = Executors.newFixedThreadPool(MAX_CONNECT_COUNT);

        mServerThread = new ServerThread();
        mServerThread.setRunning(true);
        mServerThread.start();

        mSendThread = new ServerSendThread();
        mSendThread.start();

        //startBroadcaster();
    }

    public void stop(){
        if(mServerSocket!=null) {
            try {
                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mServerThread.setRunning(false);
        //stopBroadcaster();
    }

    @Override
    public void send(@NonNull SocketInfo info, @NonNull EasyMessage data) {
        for (Socket socket:mClientList){
            if(info.getIp().equals(socket.getInetAddress().getHostAddress())
                    && info.getPort() == socket.getPort()){
                try {
                    mSendThread.putPack(data,socket.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void openBroadcaster(){
        EasyMessage message = SocketInfoMessage.getLocal();
        mBroadcaster = new Broadcaster(message,Protocol.UDP_PORT);
        mBroadcaster.start();
    }

    public void pauseBroadcaster(){
        if(mBroadcaster!=null){
            mBroadcaster.setRunning(false);
        }
    }

    public void keepBroadcaster(){
        if(mBroadcaster!=null){
            mBroadcaster.setRunning(true);
        }
    }

    public void closeBroadcaster(){
        if(mBroadcaster!=null){
            mBroadcaster.stop();
            mBroadcaster = null;
        }
    }

    public int getConnectionCount() {
        return mClientList.size();
    }

    private class ServerThread extends Thread implements ReceiveThread.OnReceiveListener {

        private boolean isRunning = false;

        public ServerThread(){
            super("ServerThread");
        }

        public void setRunning(boolean isRunning){
            this.isRunning = isRunning;
        }

        @Override
        public void run() {
            super.run();
            prepare();
            Socket mSocket = null;
            while (isRunning){
                try {
                    mSocket = mServerSocket.accept();
                    mSocket.setSoTimeout(7000);
                    mSocket.setPerformancePreferences(0, 2, 1);
                    mSocket.setTcpNoDelay(true);

                    mClientList.add(mSocket);
                    if(mConnectionListener != null){
                        mConnectionListener.onConnected(mSocket);
                    }
                    final SocketInfo info = new SocketInfo(mSocket);
                    ReceiveThread receiveThread = new ServerReceiveThread(mSocket);
                    receiveThread.setOnReceiveListener(this);
                    Log.i(TAG,info.toString() + "has connected!");
                    mExecutorService.execute(receiveThread);
                    //receiveThread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                    if(mSocket != null){
                        try {
                            mSocket.close();
                            mSocket = null;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }

        private void prepare(){
            try {
                mServerSocket = new ServerSocket(mListenerPort);
                mServerSocket.setReceiveBufferSize(1024*1024);
                //mServerSocket.setSoTimeout(8000);
            } catch (IOException e) {
                Log.i(TAG,getName() + " init error::" + e.getMessage());
            }
        }


        @Override
        public void onReceive(Thread thread, EasyMessage message) {
            if(thread instanceof ServerReceiveThread){
                ServerReceiveThread t = (ServerReceiveThread) thread;
                if(message.type == Protocol.DISCONNECT){
                    t.setRunning(false);
                    Socket socket = t.getSocket();
                    mClientList.remove(socket);
                    //断开连接
                    if(mConnectionListener != null){
                        mConnectionListener.onDisconnected(socket);
                    }
                    try {
                        close(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Socket socket = t.getSocket();
                final SocketInfo info = new SocketInfo(socket);
                if(mReceiveListener != null){
                    //Log.i(TAG,"receive " + len + " bytes data");
                    mReceiveListener.onReceive(info,message);
                }
            }
        }

        @Override
        public void onError(Thread thread, Exception e) {
            if(thread instanceof ServerReceiveThread){
                ServerReceiveThread t = (ServerReceiveThread) thread;
                Socket socket = t.getSocket();
                mClientList.remove(t.getSocket());
                //断开连接
                if(mConnectionListener != null){
                    mConnectionListener.onDisconnected(socket);
                }
                try {
                    close(socket);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }

        @Override
        public void onStopped(Thread thread) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) mExecutorService;
            executor.remove(thread);
        }

    }

    private void close(Socket socket) throws IOException {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }

    public int getPort() {
        return mListenerPort;
    }

    public void setPort(int port) {
        if(port < 1 || port > 65535)
            return;
        this.mListenerPort = port;
    }

    public interface OnReceiveListener {

        /**
         * 服务端接收到客户端请求时调用
         * @param info 数据来源的客户端信息
         * @param message 客户端发来的消息
         */
        void onReceive(SocketInfo info, EasyMessage message);

    }

    public void setOnReceiveListener(OnReceiveListener listener) {
        mReceiveListener = listener;
    }

    public interface ConnectionListener {

        void onConnected(Socket socket);

        void onDisconnected(Socket socket);
    }

    public void setConnectionListener(ConnectionListener listener) {
        mConnectionListener = listener;
    }
}
