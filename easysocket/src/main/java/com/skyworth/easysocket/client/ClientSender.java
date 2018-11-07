package com.skyworth.easysocket.client;


import android.support.annotation.NonNull;

import com.skyworth.easysocket.bean.EasyMessage;

/**
 * 作者：Ice Nation
 * 日期：2018/4/13 08:50
 * 邮箱：tangjie@skyworth.com
 */

public interface ClientSender {

    /**
     * 向服务端提交数据请求
     * @param message 已经处理好的数据包,<code>message</code>具体生产请参考
     * {@link EasyMessage.Builder}类的使用方法。
     */
    void send(@NonNull EasyMessage message);
}
