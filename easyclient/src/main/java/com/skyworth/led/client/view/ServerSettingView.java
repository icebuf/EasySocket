package com.skyworth.led.client.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.skyworth.easysocket.Utils;
import com.skyworth.led.client.R;

/**
 * Created by Ice Nation
 * 日期：2018/11/9
 * 邮箱：tangjie@skyworth.com
 */
public class ServerSettingView extends RelativeLayout {

    private EditText mIpText;

    private EditText mPortText;

    public ServerSettingView(Context context) {
        this(context,null);
    }

    public ServerSettingView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ServerSettingView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context,R.layout.view_server_setting,this);

        mIpText = findViewById(R.id.et_get_ip);
        mPortText = findViewById(R.id.et_get_port);
    }

    public String getIp() {
        return mIpText.getText().toString();
    }

    public int getPort() {
        int port = 0;
        try {
            port = Integer.parseInt(mPortText.getText().toString());
        }catch (SecurityException e){
            e.printStackTrace();
        }
        return port;
    }

    public void setIp(String ip) {
        mIpText.setText(ip);
    }

    public void setPort(int port) {
        mPortText.setText(port + "");
    }
}
