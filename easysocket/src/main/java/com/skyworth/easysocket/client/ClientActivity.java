package com.skyworth.easysocket.client;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.skyworth.easysocket.AbstractEasyActivity;
import com.skyworth.easysocket.ReceiveThread;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Ice Nation
 * 日期：2018/5/11 09:12
 * 邮箱：tangjie@skyworth.com
 */

public abstract class ClientActivity extends AbstractEasyActivity<ClientService>
        implements TCPClient.OnConnectedListener,ReceiveThread.OnReceiveListener {

    private static final int STOP_SEARCHER = 100;

    //消息发送器
    private ClientSender mSender = null;
    //服务端广播搜索器
    private ServerSearcher mSearcher = null;
    //服务端广播的信息集合
    private List<SocketInfo> mServerHosts = null;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case STOP_SEARCHER:
                    mSearcher.start();
                    onSearched(mServerHosts);
                    break;
            }
        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServerHosts = new ArrayList<>();

    }

    @Override
    protected void onServiceBind(ClientService service) {
        //设置当前activity的发送消息的源
        mSender = service.getSender();
        //设置服务中数据的接收者
        service.setOnReceiveListener(ClientActivity.this);
        Log.i(TAG,"当前接收消息者： " + TAG);

        //设置连接成功事件的接收者
        service.setOnConnectedListener(ClientActivity.this);
    }

    /**
     * 当收到服务端消息时触发此方法
     * @param message 消息内容
     */
    @Override
    public void onReceive(Thread thread,EasyMessage message) {

    }

    /**
     * 当服务端广播搜索完毕后调用
     * @param mServerHosts 搜索到的广播信息
     */
    protected void onSearched(List<SocketInfo> mServerHosts){

    }

    public ClientSender getSender(){
        return mSender;
    }

    public Handler getHandler(){
        return mHandler;
    }

    /**
     * 开始搜索服务端广播
     * @param timeOut 搜索时长
     */
    public void startServerSearcher(int timeOut){
        mServerHosts.clear();
        mSearcher = new ServerSearcher(new SearcherListener());
        mSearcher.stop();
        mHandler.sendEmptyMessageDelayed(STOP_SEARCHER,timeOut);
    }

    final class SearcherListener implements ServerSearcher.OnReceiveListener{

        @Override
        public void onFinished(List<SocketInfo> infoList) {
            mServerHosts.addAll(infoList);
        }


    }
}
