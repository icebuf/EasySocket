package com.skyworth.easysocket.server;


import android.support.annotation.NonNull;

import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;

/**
 * 作者：Ice Nation
 * 日期：2018/4/13 08:50
 * 邮箱：tangjie@skyworth.com
 */

public interface ServerSender {

    /**
     * 向服务端提交数据请求
     * @param data 已经处理好的数据包,<code>outputPackage</code>具体生产请参考
     *             {@link EasyMessage.Builder}
     *             类的使用方法。
     * @param info 数据包发送的目标客户端
     */
    void send(@NonNull SocketInfo info, @NonNull EasyMessage data);
}
