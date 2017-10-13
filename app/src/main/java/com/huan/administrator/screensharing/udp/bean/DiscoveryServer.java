
package com.huan.administrator.screensharing.udp.bean;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import android.util.Log;

public class DiscoveryServer extends Thread {
	public static final int UDP_PORT = 19999;
	public static final String tag = "domgodiscover";
	String name;

	private static final String TAG = DiscoveryServer.class.getSimpleName();

	/**
	 * 注册发现服务
	 * 
	 * @param name
	 *            服务名称标识
	 */
	public DiscoveryServer(String name) {
		this.name = name;
		this.start();
	}

	public void disconnect() {
		this.interrupt();
	}

	public void run() {
		Log.d(TAG, "UDP Discover Server start");
		DatagramSocket serverSocket = null;

		try {
			serverSocket = new DatagramSocket(UDP_PORT);
			byte[] receiveData;
			byte[] sendData;

			while (this.isInterrupted() == false) {
				receiveData = new byte[1024];
				sendData = new byte[1024];

				try {
					Log.v(TAG, "UDP Discover Server Listenning");
					DatagramPacket receivePacket = new DatagramPacket(receiveData,
							receiveData.length);
					serverSocket.receive(receivePacket);
					String data = new String(receivePacket.getData());

					if (data != null)
						Log.v(TAG, "receive data: "
								+ data.substring(0, receivePacket.getLength()).trim());

					if (data != null
							&& data.substring(0, receivePacket.getLength()).trim().equals(tag)) {
						sendData = (name + "," + "additional info").getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
								receivePacket.getAddress(), receivePacket.getPort());
						serverSocket.send(sendPacket);
					}

				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			try {
				if (serverSocket != null)
					serverSocket.close();
			} catch (Exception e2) {
				Log.e(TAG, e2.toString());
			}
		}

	}
}
