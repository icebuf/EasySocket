package com.skyworth.easysocket.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.Socket;

/**
 * 作者：Ice Nation
 * 日期：2018/5/8 14:47
 * 邮箱：tangjie@skyworth.com
 */


public class SocketInfo implements Parcelable{

    public static final String TAG = "SocketInfo";

    private String hostName;

    private String ip;

    private int port;

    public SocketInfo(Socket socket) {
        hostName = socket.getInetAddress().getHostName();
        ip = socket.getInetAddress().getHostAddress();
        port = socket.getPort();
    }

    public SocketInfo() {
        hostName = "null";
        ip = "";
    }

    public SocketInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "SocketInfo{" +
                "hostName='" + hostName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hostName);
        dest.writeString(ip);
        dest.writeInt(port);
    }

    /**
     * 负责反序列化
     */
    public static final Creator<SocketInfo> CREATOR = new Creator<SocketInfo>() {
        /**
         * 从序列化后的对象中创建原始对象
         */
        @Override
        public SocketInfo createFromParcel(Parcel source) {
            return new SocketInfo(source);
        }

        /**
         * 创建指定长度的原始对象数组
         */
        @Override
        public SocketInfo[] newArray(int size) {
            return new SocketInfo[size];
        }
    };

    public SocketInfo(Parcel parcel) {
        hostName = parcel.readString();
        ip = parcel.readString();
        port = parcel.readInt();
    }
}
