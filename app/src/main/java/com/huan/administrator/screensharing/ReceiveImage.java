package com.huan.administrator.screensharing;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class ReceiveImage {
    private static int IMAGE_PORT = 6891;
    private DatagramPacket datagramPacket;
    private DatagramSocket datagramSocket;

    private static  ReceiveImage receiveImage;
    public static ReceiveImage getInstance(){
        if(receiveImage == null){
            receiveImage = new ReceiveImage();
        }
        return receiveImage;
    }
    byte b[] = new byte[6892];
    public Bitmap receive() throws SocketException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(datagramSocket == null){
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(new InetSocketAddress(IMAGE_PORT));
        }else
            try {
                datagramSocket = new DatagramSocket(IMAGE_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        while(true){
            datagramPacket = new DatagramPacket(b, b.length);
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String msg  = new String(datagramPacket.getData(),0,datagramPacket.getLength());
            if(msg.startsWith(";!")){
                System.out.println("-->接收到所有数据");
                break;
            }
            baos.write(datagramPacket.getData(),0,datagramPacket.getLength());
            //System.out.println("-->正在接收数据:"+datagramPacket.getData());
        }
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Bytes2Bitmap(baos.toByteArray());

    }


    // byte[]转换成Bitmap
    public static Bitmap Bytes2Bitmap(byte[] b) {
        try {

            if (b.length != 0) {
                return BitmapFactory.decodeByteArray(b, 0, b.length);
            }
        } catch (Exception e) {
        }
        return null;
    }

}