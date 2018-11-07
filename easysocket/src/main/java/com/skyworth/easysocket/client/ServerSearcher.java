package com.skyworth.easysocket.client;


import android.util.Log;

import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.Utils;
import com.skyworth.easysocket.bean.EasyMessage;
import com.skyworth.easysocket.bean.SocketInfo;
import com.skyworth.easysocket.bean.SocketInfoMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Code for dealing with MobileTeaching server discovery. This class tries to send a
 * broadcast UDP packet over your wifi network to discover the MobileTeaching service.
 * 
 * @author max
 * 
 */

public class ServerSearcher {

	private static final String TAG = "ServerSearcher";

	private static final int RCV_BUFFER_SIZE = 1024;

	private static final String MSG_HEAD = Protocol.UDP_MESSAGE_HEAD;
	private static final int PORT = Protocol.UDP_PORT;

	private OnReceiveListener mReceiver = null;
	private DatagramSocket searchSocket = null;

	private List<SocketInfo> infoList = null;

	private boolean isRunning = false;
	
	public interface OnReceiveListener {

		//完成服务端搜索，产生一个列表
		void onFinished(List<SocketInfo> infoList);
	}
	
	public ServerSearcher(OnReceiveListener mReceiver) {
		this.mReceiver = mReceiver;
		infoList = new ArrayList<>();
	}
	
	public void start() {
		isRunning = true;
		try {
			searchSocket = new DatagramSocket(PORT);
			searchSocket.setBroadcast(true);
			searchSocket.setSoTimeout(1000);
		} catch (IOException e) {
			Log.d(getClass().getSimpleName(),e.getMessage());
			return;
		}
		new Thread() {
			@Override
			public void run() {

				byte[] readBuffer = new byte[RCV_BUFFER_SIZE];
				infoList.clear();

				while (isRunning) {
					try {

						DatagramPacket rcvPacket = new DatagramPacket(readBuffer, readBuffer.length);
						searchSocket.receive(rcvPacket);

						byte[] readData = rcvPacket.getData();
						int rcvLen = rcvPacket.getLength();

						if (rcvLen <= 8)
							continue;
						int head = Utils.readInt(readData,0);

						if(head != Protocol.HEAD)
							continue;
						int len = Utils.readInt(readData,4);

						if(len <= 0 || len > rcvLen - 8)
							continue;

						byte[] bytes = new byte[len];
						System.arraycopy(readData,8,bytes,0,len);

						EasyMessage easyMessage = new EasyMessage(bytes,len);
						SocketInfoMessage infoMessage = new SocketInfoMessage(easyMessage);
						if(!infoMessage.verifyInfo())
							continue;

						SocketInfo info = infoMessage.getServerInfo();

						if(!included(info)) {
							infoList.add(infoMessage.getServerInfo());
						}

					} catch (IOException e) {
						Log.e(TAG,"" + e.getMessage());
					}
				}
				if(mReceiver != null) {
					mReceiver.onFinished(infoList);
				}
			}
		}.start();

	}

	private boolean included(SocketInfo info) {
		for (SocketInfo temp:infoList){
			if(temp.getIp().equals(info.getIp()))
				return true;
		}
		return false;
	}

	public void stop() {
		isRunning = false;
		if(searchSocket != null) {
			searchSocket.disconnect();
			searchSocket.close();
		}

	}

}
