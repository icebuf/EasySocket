package com.skyworth.easysocket.bean;


import android.support.annotation.NonNull;
import android.util.Log;

import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.Utils;

/**
 * 作者：Ice Nation
 * 日期：2018/5/11 16:11
 * 邮箱：tangjie@skyworth.com
 */

public class SocketInfoMessage extends EasyMessage {


    private SocketInfo info = null;

    public SocketInfoMessage(EasyMessage message) {
        super(message);


        info = new SocketInfo();
        setPosition(START_DATA);
        int nameLen = nextInt();
        info.setHostName(nextString(nameLen));
        int ipLen = nextInt();
        info.setIp(nextString(ipLen));
        info.setPort(nextInt());

        Log.i("222",info.toString());
    }

    public static EasyMessage getLocal() {
        SocketInfo info = new SocketInfo();
        info.setIp(Utils.getHostIp());
        info.setHostName(Utils.getHostName());
        return create(info);
    }

    public SocketInfo getServerInfo(){
        return info;
    }


    public static EasyMessage create(@NonNull SocketInfo info){
        return new Builder()
                .setType(Protocol.UDP_TYPE)
                .setCode(Protocol.UDP_CODE)
                .addData(info.getHostName())
                .addData(info.getIp())
                .addData(info.getPort())
                .build();

    }

    public boolean verifyInfo() {
        return Utils.isIP(info.getIp());
    }
}
