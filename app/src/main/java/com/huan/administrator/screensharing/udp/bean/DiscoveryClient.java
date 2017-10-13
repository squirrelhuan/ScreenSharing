
package com.huan.administrator.screensharing.udp.bean;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import android.net.wifi.WifiManager;
import android.util.Log;

public class DiscoveryClient {
	private static final String TAG = DiscoveryClient.class.getSimpleName();
	
	/** 默认不需要发现本机的ip */
	private static final boolean defaultExcludeSelf = true;

	
	/**
	 * UDP发现的静态方法，默认不需要发现本机
	 * 
	 * @param mWifi WifiManager
	 * @return ArrayList<DiscoveryServerInfo> 
	 *         返回发现到的主机列表
	 */
	public static ArrayList<UdpServerInfo> findServer(WifiManager mWifi) {
		return DiscoveryClient.findServer(mWifi, DiscoveryServer.UDP_PORT, DiscoveryServer.tag,
				defaultExcludeSelf);
	}

	/**
	 * UDP发现的静态方法，根据参数来判断是否需要发现本机
	 * 
	 * @param mWifi WifiManager
	 * @param excludeSelf boolean: true(不需要发现本机), false(需要发现本机)
	 * @return ArrayList<DiscoveryServerInfo>
	 *         返回发现到的主机列表
	 */
	public static ArrayList<UdpServerInfo> findServer(WifiManager mWifi,
			boolean excludeSelf) {
		return DiscoveryClient.findServer(mWifi, DiscoveryServer.UDP_PORT, DiscoveryServer.tag,
				excludeSelf);
	}

	public static ArrayList<UdpServerInfo> findServer(WifiManager mWifi, int port, String tag,
			boolean excludeSelf) {
		String localIp = null;
		if (excludeSelf) {
			localIp = Utils.getLocalIp(mWifi);
		}

		ArrayList<UdpServerInfo> result = new ArrayList<UdpServerInfo>();

		try {
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setBroadcast(true);
			InetAddress IPAddress = Utils.getBroadcastAddress(mWifi);
			Log.d(TAG, "broadcast address " + IPAddress.getHostAddress());
			
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];

			sendData = tag.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress,
					port);
			Log.d(TAG, "sent " + tag);
			clientSocket.send(sendPacket);

			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime <= 5000) // 5000ms
			{
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

				try {
					clientSocket.setSoTimeout(500);
					clientSocket.receive(receivePacket);

					if (receivePacket.getAddress() != null
							&& receivePacket.getAddress().getHostAddress() != null) {
						String discoveredName, discoveredIp;
						String discoveredInfo;
						byte[] dataByte = receivePacket.getData();
						String received = new String(dataByte);

						if (received != null) {
							received = received.substring(0, receivePacket.getLength()).trim();
							StringTokenizer st = new StringTokenizer(received, ",");

							try {
								discoveredName = st.nextToken();
								discoveredIp = receivePacket.getAddress().getHostAddress();
								discoveredInfo = st.nextToken();

								Log.d(TAG, "discovered " + discoveredName + ", " + discoveredIp
										+ ":" + discoveredInfo);

								boolean existed = false;
								if (result.size() > 0) {
									for (UdpServerInfo info : result) {
										if (info != null && info.ip.equals(discoveredIp)) {
											existed = true;
											break;
										}
									}
								}

								if (!existed && !discoveredIp.equals(localIp)) {
									result.add(new UdpServerInfo(discoveredName, discoveredIp,
											discoveredInfo));
								}
							} catch (NoSuchElementException nsee) {
								Log.d(TAG, nsee.getLocalizedMessage());
							}

						}
					}

				} catch (SocketTimeoutException e) {
					Log.e(TAG, e.toString());
				}
			}

			clientSocket.close();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		return result;
	}
}
