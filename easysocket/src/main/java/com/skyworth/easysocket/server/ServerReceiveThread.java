package com.skyworth.easysocket.server;

import com.skyworth.easysocket.ReceiveThread;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 作者：Ice Nation
 * 日期：2018/11/7 17:55
 * 邮箱：tangjie@skyworth.com
 */
public class ServerReceiveThread extends ReceiveThread {

    private Socket mSocket = null;

    public ServerReceiveThread(Socket socket) throws IOException{
        super(new DataInputStream(socket.getInputStream()));
        mSocket = socket;
    }

    public Socket getSocket() {
        return mSocket;
    }
}
