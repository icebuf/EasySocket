package com.skyworth.easysocket.client;


import android.util.Log;

import com.skyworth.easysocket.AbstractService;
import com.skyworth.easysocket.ReceiveThread;
import com.skyworth.easysocket.bean.SocketInfo;

public class ClientService extends AbstractService {

    private TCPClient mClient = null;

    public ClientService() {
        super();
        mClient = new TCPClient();
    }

    public void connectHost(SocketInfo info) {
        if (info == null) {
            //如果信息为空
            return;
        }

        mClient.setServerInfo(info);
        mClient.connect();
        Log.i(TAG,"正在连接："+info.toString());
    }

    public void disconnect(){
        mClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mClient!=null){
            mClient.disconnect();
            mClient = null;
        }
    }

    public void setOnReceiveListener(ReceiveThread.OnReceiveListener listener){
        mClient.setOnReceiveListener(listener);
    }

    public void setOnConnectedListener(TCPClient.OnConnectedListener listener) {
        mClient.setOnConnectedListener(listener);
    }

    public ClientSender getSender() {
        return mClient;
    }

    public boolean isConnected() {
        if(mClient == null)
            return false;
        return mClient.isConnected();
    }
}
