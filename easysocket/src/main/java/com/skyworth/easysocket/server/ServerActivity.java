package com.skyworth.easysocket.server;

import android.app.Service;
import android.util.Log;

import com.skyworth.easysocket.AbstractEasyActivity;
import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;


/**
 * 作者：Ice Nation
 * 日期：2018/5/11 15:05
 * 邮箱：tangjie@skyworth.com
 */

public abstract class ServerActivity extends AbstractEasyActivity<ServerService>
        implements TCPServer.OnReceiveListener {

    private ServerSender mSender = null;


    @Override
    protected void onServiceBind(ServerService service) {
        //设置当前activity的发送消息的源
        mSender = service.getSender();
        //设置服务中数据的接收者
        service.setOnReceiveListener(ServerActivity.this);
        Log.i(TAG,"当前接收消息者： " + TAG);
    }

    @Override
    public void onReceive(SocketInfo info, EasyMessage message) {
        if(message.type == Protocol.HEART)
            mSender.send(info, message);
    }

    @Override
    protected Class<? extends Service> getServiceClass() {
        return ServerService.class;
    }

    public ServerSender getSender() {
        return mSender;
    }
}
