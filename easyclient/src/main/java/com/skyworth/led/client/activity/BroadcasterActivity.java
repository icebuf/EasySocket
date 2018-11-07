package com.skyworth.led.client.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.skyworth.easysocket.ReceiveThread;
import com.skyworth.easysocket.Utils;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;
import com.skyworth.easysocket.client.TCPClient;
import com.skyworth.led.client.R;

import java.util.Arrays;
import java.util.List;

public class BroadcasterActivity extends AppCompatActivity {

    private TCPClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcaster);

        final TextView textView = findViewById(R.id.tv_local_ip);

        List<String> ips = Utils.listHostIp();
        textView.setText(Arrays.toString(ips.toArray()));


        mClient = new TCPClient();
        mClient.setServerInfo(new SocketInfo("10.0.2.2", 6100));
        mClient.connect();
        mClient.setOnReceiveListener(new ReceiveThread.OnReceiveListener() {
            @Override
            public void onReceive(EasyMessage message) {
                Log.i("onReceive():",message.type + " " + message.code);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mClient.disconnect();
        super.onDestroy();
    }
}
