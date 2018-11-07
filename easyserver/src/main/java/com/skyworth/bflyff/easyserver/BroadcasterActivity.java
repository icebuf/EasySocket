package com.skyworth.bflyff.easyserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.skyworth.easysocket.Utils;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;
import com.skyworth.easysocket.server.TCPServer;

import java.util.Arrays;
import java.util.List;

public class BroadcasterActivity extends AppCompatActivity implements TCPServer.OnReceiveListener {

    private TCPServer mServer;
    private TextView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_broadcaster);

        mLogView = findViewById(R.id.tv_receive);

        List<String> ips = Utils.listHostIp();
        mLogView.setText(Arrays.toString(ips.toArray()));

        mServer = new TCPServer(7100);
        mServer.start();
        mServer.setOnReceiveListener(this);
    }

    @Override
    public void onReceive(final SocketInfo info, final EasyMessage message) {
        mLogView.post(new Runnable() {
            @Override
            public void run() {
                String str = mLogView.getText().toString();
                mLogView.setText(str + "\n" + info.toString() + " size = " + message.getBytes().length);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mServer.stop();
        super.onDestroy();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
