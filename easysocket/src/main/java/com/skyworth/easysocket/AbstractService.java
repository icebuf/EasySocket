package com.skyworth.easysocket;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 作者：Ice Nation
 * 日期：2018/5/11 14:25
 * 邮箱：tangjie@skyworth.com
 */

public abstract class AbstractService extends Service {

    protected final String TAG = getClass().getSimpleName();

    protected LocalBinder mBinder = null;

    public AbstractService() {
        mBinder = new LocalBinder();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind()::"+intent.getStringExtra("name"));
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Connect service started!");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 解除绑定时调用
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind()::" + intent.getStringExtra("name"));
        return super.onUnbind(intent);
    }


    /**
     * 创建Binder对象，返回给客户端即Activity使用，提供数据交换的接口
     */
    public class LocalBinder extends Binder {
        // 声明一个方法，getService。（提供给客户端调用）
        public AbstractService getService() {
            // 返回当前对象LocalService,这样我们就可在客户端端调用Service的公共方法了
            return AbstractService.this;
        }
    }

}
