package com.skyworth.bflyff.server;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ScrollView;
import android.widget.TextView;

import com.skyworth.easysocket.Utils;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;
import com.skyworth.easysocket.server.TCPServer;

import java.net.Socket;

public class MainActivity extends AppCompatActivity implements TCPServer.OnReceiveListener {

    public static final int SERVER_PORT = 7100;

    private TCPServer mServer;
    private TextView mIpView;
    private TextView mPortView;
    private TextView mCountView;
    private TextView mMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIpView = findViewById(R.id.tv_server_ip);
        mPortView = findViewById(R.id.tv_server_port);
        mCountView = findViewById(R.id.tv_connection_count);
        mMessageView = findViewById(R.id.tv_receive_msg);

        mIpView.setText(getString(R.string.ip_info,Utils.getHostIp()));
        mPortView.setText(getString(R.string.port_info,SERVER_PORT));
        mCountView.setText(getString(R.string.connection_count_info,0));

        mServer = new TCPServer(SERVER_PORT);
        mServer.start();
        mServer.setOnReceiveListener(this);
        mServer.setConnectionListener(new TCPServer.ConnectionListener() {
            @Override
            public void onConnected(Socket socket) {
                mCountView.post(new Runnable() {
                    @Override
                    public void run() {
                        mCountView.setText(getString(R.string.connection_count_info,
                                mServer.getConnectionCount()));
                    }
                });

            }

            @Override
            public void onDisconnected(Socket socket) {
                mCountView.post(new Runnable() {
                    @Override
                    public void run() {
                        mCountView.setText(getString(R.string.connection_count_info,
                                mServer.getConnectionCount()));
                    }
                });
            }
        });
    }

    @Override
    public void onReceive(final SocketInfo info, final EasyMessage message) {
        mMessageView.post(new Runnable() {
            @Override
            public void run() {
                String str = mMessageView.getText().toString();
                mMessageView.setText(str + "\n[" + info.getIp() + "]: " + message.toString());

                ScrollView scrollView = findViewById(R.id.scroll_log);
                scrollView.scrollTo(0, 1000);
            }
        });

        mServer.send(info,message);
    }

    @Override
    protected void onDestroy() {
        mServer.stop();
        super.onDestroy();
    }
}
