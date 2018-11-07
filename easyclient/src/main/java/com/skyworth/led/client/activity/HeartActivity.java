package com.skyworth.led.client.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.skyworth.easysocket.ReceiveThread;
import com.skyworth.easysocket.Utils;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;
import com.skyworth.easysocket.client.TCPClient;
import com.skyworth.led.client.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HeartActivity extends AppCompatActivity {

    public static final int SERVER_PORT = 6100;

    private TCPClient mClient;
    private TextView mIpView;
    private TextView mPortView;
    private Button mConnectView;
    private TextView mMessageView;

    private CheckBox mEmulatorBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart);

        mIpView = findViewById(R.id.tv_server_ip);
        mPortView = findViewById(R.id.tv_server_port);
        mConnectView = findViewById(R.id.btn_connect);
        mMessageView = findViewById(R.id.tv_receive_msg);
        mEmulatorBox = findViewById(R.id.cb_is_emulator);

        mIpView.setText(getString(R.string.ip_info,getIp()));
        mPortView.setText(getString(R.string.port_info,SERVER_PORT));

        mConnectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                if(mClient.isConnected())
                    mClient.disconnect();
                else {
                    mClient.connect();
                }
            }
        });

        mEmulatorBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mClient.setServerInfo(new SocketInfo(getIp(), SERVER_PORT));
                mIpView.setText(getString(R.string.ip_info,getIp()));
            }
        });

        mClient = new TCPClient();
        mClient.setServerInfo(new SocketInfo(getIp(), SERVER_PORT));
        mClient.setOnReceiveListener(new ReceiveThread.OnReceiveListener() {

            @Override
            public void onReceive(Thread thread, EasyMessage message) {
                //设置日期格式
                SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss",
                        Locale.getDefault());
                String date = df.format(new Date());
                putLog("[" + date + "]: " + message.toString());
            }

            @Override
            public void onError(Thread thread, Exception e) {

            }
        });
        mClient.setOnConnectedListener(new TCPClient.OnConnectedListener() {
            @Override
            public void onConnected(int port, String ip) {
                mConnectView.post(new Runnable() {
                    @Override
                    public void run() {
                        mConnectView.setClickable(true);
                        mConnectView.setText(R.string.btn_text_disconnect);
                        mEmulatorBox.setEnabled(false);
                    }
                });
            }

            @Override
            public void onConnectFail(Exception e) {

            }

            @Override
            public void onDisconnected(int port, String ip) {
                mConnectView.post(new Runnable() {
                    @Override
                    public void run() {
                        mConnectView.setClickable(true);
                        mConnectView.setText(R.string.btn_text_connected);
                        mEmulatorBox.setEnabled(true);
                    }
                });
            }

            @Override
            public void onReconnected(int port, String ip) {

            }
        });
    }

    private void putLog(final String log){
        mMessageView.post(new Runnable() {
            @Override
            public void run() {
                int count = mMessageView.getLineCount();
                if(count < 200){
                    mMessageView.append(log + "\n");
                }else {
                    mMessageView.setText(log + "\n");
                }
                ScrollView scrollView = findViewById(R.id.scroll_log);
                scrollView.smoothScrollTo(0, mMessageView.getBottom());
            }
        });
    }

    private String getIp() {
        //adb –s emulator-5554 forward tcp:6100 tcp:7100
        if(mEmulatorBox.isChecked())
            return "10.0.2.2";
        else return Utils.getHostIp();
    }

    @Override
    protected void onDestroy() {
        mClient.disconnect();
        super.onDestroy();
    }
}
