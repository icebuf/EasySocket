package com.skyworth.led.client.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.skyworth.easysocket.ReceiveThread;
import com.skyworth.easysocket.Utils;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;
import com.skyworth.easysocket.client.TCPClient;
import com.skyworth.led.client.R;
import com.skyworth.led.client.view.ServerSettingView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HeartActivity extends AppCompatActivity {

    public static final int SERVER_PORT = 6100;
    public String mServerIp = "192.168.0.101";

    private static final String TAG = HeartActivity.class.getSimpleName();

    private TCPClient mClient;
    private TextView mIpView;
    private TextView mPortView;
    private TextView mServerIpView;
    private TextView mServerPortView;
    private Button mConnectView;
    private TextView mMessageView;

    private CheckBox mEmulatorBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mIpView = findViewById(R.id.tv_local_ip);
        mPortView = findViewById(R.id.tv_local_port);
        mServerIpView = findViewById(R.id.tv_server_ip);
        mServerPortView = findViewById(R.id.tv_server_port);
        mConnectView = findViewById(R.id.btn_connect);
        mMessageView = findViewById(R.id.tv_receive_msg);
        mEmulatorBox = findViewById(R.id.cb_is_emulator);

        mIpView.setText(getString(R.string.ip_info,getIp()));

        mServerIpView.setText(getString(R.string.server_ip_info,mServerIp));
        mServerPortView.setText(getString(R.string.port_info,SERVER_PORT));

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

        mConnectView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openSettingDialog();
                return true;
            }
        });

        mEmulatorBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mClient.setServerInfo(new SocketInfo(getServerIp(), SERVER_PORT));
                mIpView.setText(getString(R.string.ip_info,getIp()));
                mServerIpView.setText(getString(R.string.server_ip_info,getServerIp()));
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

            @Override
            public void onStopped(Thread thread) {

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
                        mPortView.setText(getString(R.string.port_info,mClient.getLocalPort()));
                    }
                });
            }

            @Override
            public void onConnectFail(Exception e) {
//                Toast.makeText(HeartActivity.this,R.string.tcp_connect_fail,Toast.LENGTH_SHORT).show();
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



    private void openSettingDialog() {
        final ServerSettingView settingView = new ServerSettingView(this);
        settingView.setIp(mClient.getServerIp());
        settingView.setPort(mClient.getServerPort());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.server_info_setting)
                .setView(settingView)
                .setNegativeButton(R.string.btn_text_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip = settingView.getIp();
                        int port = settingView.getPort();
                        if(Utils.isIP(ip) && Utils.isPort(port)){
                            Log.i(TAG,"IP:" + ip + " PORT:" + port);
                            mServerIpView.setText(ip);
                            mServerPortView.setText(port + "");
                            mClient.setServerInfo(ip,port);
                            dialog.dismiss();
                        }else {
                            Toast.makeText(HeartActivity.this,"配置信息错误!",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setPositiveButton(R.string.btn_text_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();

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

    private String getServerIp() {
        if(mEmulatorBox.isChecked())
            return "10.0.2.2";
        else return mServerIp;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_heart,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_clear_log:
                mMessageView.setText("");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mClient.disconnect();
        super.onDestroy();
    }
}
