package com.skyworth.easysocket;


import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * 作者：Ice Nation
 * 日期：2018/5/11 14:50
 * 邮箱：tangjie@skyworth.com
 */

public abstract class AbstractEasyActivity<T extends AbstractService> extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName();
    protected Activity mActivity = null;

    private ServiceConnection mConnection = null;
    private T mService = null;
    private WifiReceiver mReceiver = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = AbstractEasyActivity.this;
        mConnection = new MyServiceConnection();

        //注册WIFI监听广播
        mReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        //filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        //filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);

        //启动长连接服务
        startService();
    }

    protected void startService(){
        //创建绑定对象
        final Intent intent = new Intent(mActivity, getServiceClass());
        intent.putExtra("name",TAG);
        //调用绑定方法
        mActivity.bindService(intent, mConnection, Service.BIND_AUTO_CREATE);
        //启动服务
        mActivity.startService(intent);
    }

    protected abstract Class<? extends Service> getServiceClass();

    class MyServiceConnection implements ServiceConnection{
        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "绑定成功");
            // 获取Binder
            AbstractService.LocalBinder binder = (AbstractService.LocalBinder) service;
            AbstractEasyActivity.this.mService = (T) binder.getService();
            onServiceBind(AbstractEasyActivity.this.mService);
        }
        /**
         * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
         * 例如内存的资源不足时这个方法才被自动调用。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    /**
     * 接收WIFI打开和关闭的广播
     */
    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            if(wifiState == WifiManager.WIFI_STATE_DISABLED){
                onWifiClose();
            }else if(wifiState == WifiManager.WIFI_STATE_ENABLED){
                onWifiOpen();
            }
        }
    }

    /**
     * 当WIFI打开时调用此方法，在子类中重写该方法实现不同需求
     */
    protected abstract void onWifiOpen();
    /**
     * 当WIFI打关闭调用此方法，在子类中重写该方法实现不同需求
     */
    protected abstract void onWifiClose();

    /**
     * 当Activity与服务绑定时调用
     * @param service 绑定的服务
     */
    protected abstract void onServiceBind(T service);

    protected void showToast(int resId){
        showToast(getString(resId));
    }

    protected void showToast(String msg){
        Toast.makeText(mActivity,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        unregisterReceiver(mReceiver);
        Log.i(TAG,"解除服务绑定");
    }

    protected T getService(){
        return mService;
    }

    protected WifiReceiver getWifiReceiver(){
        return mReceiver;
    }

    public ServiceConnection getServiceConnection() {
        return mConnection;
    }

}