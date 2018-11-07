package com.skyworth.easysocket.server;


import com.skyworth.easysocket.AbstractService;
import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.Utils;
import com.skyworth.easysocket.bean.SocketInfo;

public class ServerService extends AbstractService {

    private TCPServer mServer = null;
    private SocketInfo mSocketInfo = null;

    private boolean isRunning = false;

    public ServerService() {
        super();
        initInfo();
    }

    private void initInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSocketInfo = new SocketInfo();
                mSocketInfo.setIp(Utils.getHostIp());
                mSocketInfo.setHostName(Utils.getHostName());
                mSocketInfo.setPort(Protocol.TCP_PORT);
            }
        }).start();
    }

    public void startServer(final int port){
        if(isRunning)
            return;

        if(mSocketInfo == null)
            return;

        if(mServer == null)
            mServer = new TCPServer(port);
        else mServer.setPort(port);
        mServer.start();
        isRunning = true;
    }

    public void stopServer(){
        mServer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServer();
    }

    public ServerSender getSender() {
        return mServer;
    }

    public SocketInfo getSocketInfo(){
        return mSocketInfo;
    }

    public void setOnReceiveListener(TCPServer.OnReceiveListener serverReceiver) {
        mServer.setOnReceiveListener(serverReceiver);
    }
}
