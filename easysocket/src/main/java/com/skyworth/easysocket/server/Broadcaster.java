package com.skyworth.easysocket.server;


import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.Utils;
import com.skyworth.easysocket.bean.EasyMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import static com.skyworth.easysocket.Utils.INT_BYTES;

/**
 * 作者：Ice Nation
 * 日期：2018/5/11 15:56
 * 邮箱：tangjie@skyworth.com
 */

public class Broadcaster {


    private DatagramSocket mUdpSocket = null;

    private Timer mTimer = null;

    private int port;

    private EasyMessage mMessage = null;

    private boolean isRunning = true;

    public Broadcaster(EasyMessage message,int port) {
        this.port = port;
        this.mMessage = message;
    }

    public void setRunning(boolean b) {
        isRunning = b;
    }

    public void start(){
        mTimer = new Timer("Broadcaster");
        mTimer.schedule(new UDPTimerTask(),0,500);

    }

    public void stop(){
        if(mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void sendBroadcast(){
        try {

            /*这一步就是将本机的IP地址转换成xxx.xxx.xxx.255*/
            String localIp = Utils.getHostIp();
            int lastPoint = localIp.lastIndexOf(".");
            String broadCastIP = localIp.substring(0,lastPoint + 1) + "255";
            InetAddress ipAddress = InetAddress.getByName(broadCastIP);

            //Log.i("222",mInfo.getIp() + " " + broadCastIP);

            mUdpSocket = new DatagramSocket(port);// 创建用来发送数据报包的套接字

            int msgLen = mMessage.getBytes().length;
            byte[] bytes = new byte[msgLen + 8];
            Utils.putInt(bytes,0,Protocol.HEAD);
            Utils.putInt(bytes,INT_BYTES,msgLen);
            System.arraycopy(mMessage.getBytes(),0,bytes,8,msgLen);

            DatagramPacket dp = new DatagramPacket(bytes, bytes.length,ipAddress,port);

            mUdpSocket.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            mUdpSocket.close();
            mUdpSocket = null;
        }
    }

    class UDPTimerTask extends TimerTask {

        @Override
        public void run() {
            //暂停时计数器归零
            if(!isRunning)
                return;
            sendBroadcast();
        }
    }
}
