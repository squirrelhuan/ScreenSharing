package com.huan.administrator.screensharing;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.huan.mylog.MyLog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/5/18.
 */

public class MainActivity3 extends Activity {

    static ImageView imageView, iv_bg;
    static Context mContext;

    private static Thread receive_Thread;

    RevImageThread revImageThread;
    private static final int COMPLETED = 0x111;
    Thread receiveThread;
    Runnable runnable,runnable4;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mContext = this;
        imageView = (ImageView) findViewById(R.id.iv_show);
        iv_bg = (ImageView) findViewById(R.id.iv_bg);
        MyLog.i("running start");

        handler = new MyHandler();
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                MyLog.i("running start...");
                while (true) {
                    try {
                        receive();
                    } catch (Exception e) {
                        MyLog.a(e.getMessage());
                    }
                }
            }
        });
        receiveThread.start();
        Toast.makeText(this, "初始化完成", Toast.LENGTH_SHORT).show();


        handler4 = new Handler();
        runnable4 = new Runnable() {
            @Override
            public void run() {
                        comebackTime--;
                        if(comebackTime<0){
                            // 需要做的事:发送消息
                            Message message = new Message();
                            message.what = 104;
                            handler.sendMessage(message);
                        }
                handler4.postDelayed(runnable4, 1000);
            }
        };
        handler4.postDelayed(runnable4, 1000);
        // revImageThread = new RevImageThread(handler);
        //new Thread(revImageThread).start();
        // initSocket();

    }

    static int comebackTime = 30;

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.i("onResume");
    }

    private static int IMAGE_PORT = 26891;
    private static DatagramPacket datagramPacket;
    private static DatagramSocket datagramSocket;

    static byte b[] = new byte[5120];
    //向UI线程发送消息
    private static Handler handler,handler4;

    public static byte[] receive() throws SocketException {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (datagramSocket == null) {
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(new InetSocketAddress(IMAGE_PORT));
        } else
            try {
                datagramSocket = new DatagramSocket(IMAGE_PORT);
            } catch (SocketException e) {
                //e.printStackTrace();
            }

        while (true) {
            datagramPacket = new DatagramPacket(b, b.length);
            try {
                datagramSocket.receive(datagramPacket);
                comebackTime = 30;
            } catch (IOException e) {
                e.printStackTrace();
            }

            MyLog.i("recive");
            System.out.println("recive");
            String msg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
            if (msg.startsWith(";!")) {
                MyLog.i("接收到所有数据");
                System.out.println("-->接收到所有数据");
               /* Message message = new Message();
                message.obj = "接收到所有数据";
                uiHandler.sendMessage(message);*/

                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                bitmap = Bytes2Bitmap(baos.toByteArray());
                byte data[] = baos.toByteArray();
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                Message message = new Message();
                message.what = COMPLETED;
                message.obj = bitmap;
                handler.sendMessage(message);

                break;
            } else {
                // Message message = new Message();
                //message.obj = "receive";
                //uiHandler.sendMessage(message);
                baos.write(datagramPacket.getData(), 0, datagramPacket.getLength());
                //System.out.println("-->正在接收数据:"+datagramPacket.getData());
            }


        }
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();


    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
               // Toast.makeText(mContext, "刷新图片", Toast.LENGTH_SHORT).show();
                bitmap = (Bitmap) msg.obj;
                //imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
                Resources res = mContext.getResources();
                //bitmap=BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
                if (bitmap != null) {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                    iv_bg.setVisibility(View.GONE);
                }
                super.handleMessage(msg);
            }else if(msg.what==104){
                imageView.setVisibility(View.GONE);
                iv_bg.setVisibility(View.VISIBLE);
            }
        }
    }


    private static DatagramSocket socket = null;
    public static final int LOCAL_PORT = 6891;

    private void initSocket() {
        try {
            if (socket == null) {
                socket = new DatagramSocket(LOCAL_PORT);
                //Toast.makeText(getApplicationContext(), "newSocket...", 3000).show();
            } else {
                //Toast.makeText(getApplicationContext(), "initSocket...", 3000).show();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        // 不停地接收内容
        Executors.newCachedThreadPool().execute(new ReceiveDataRunnable());

        // 通知上线
        // 发送一条数据告诉服务器我上线了
        // sendData(a);
        // 上线通知代替心跳，3秒一次上线通知
        // Executors.newCachedThreadPool().execute(new SendheartbeatRunnable());
        // sendData();
        // 2秒自动发送一次获取机器列表信息
        // Executors.newCachedThreadPool().execute(new SendHeartRunnable());
    }


    static Bitmap bitmap = null;

    // 接收所有通信返回的数据
    private class ReceiveDataRunnable implements Runnable {
        @Override
        public void run() {


            try {
                MyLog.i("start");
                ReceiveImage.getInstance().receive();
            } catch (Exception e) {
                MyLog.i(e.getMessage());
            }

            Message message = new Message();
            byte data1[] = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 参数一:要接受的data 参数二：data的长度
            DatagramPacket packet1 = new DatagramPacket(data1, data1.length);
            // 把接收到的data转换为String字符串
            while (true) {
                try {
                    socket.receive(packet1);
                    Log.i("CGQ", "recieve success:" + result);

                    String recvStr = new String(packet1.getData(), 0,
                            packet1.getLength());
                    if (recvStr.startsWith(";!")) {
                        System.out.println("-->接收到所有数据");
                        baos.close();

                        result = baos.toByteArray();
                        message.obj = "result:" + result.length;
                        uiHandler.sendMessage(message);

                        if (result != null && result.length > 0) {
                            bitmap = Bytes2Bitmap(result);
                        }

                        if (bitmap != null) {
                            message.obj = "UI";
                            uiHandler.sendMessage(message);
                        } else {
                            message.obj = "bitmap null";
                        }
                        continue;
                    }
                    baos.write(packet1.getData(), 0, packet1.getLength());

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    MyLog.a(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    MyLog.a(e.getMessage());
                }
            }
        }
    }

    private static DatagramSocket client;

    static Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj != null)
                Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            if (bitmap != null && msg.obj.equals("UI")) {
                Toast.makeText(mContext, "更新图片", Toast.LENGTH_SHORT).show();
                imageView.setImageBitmap(bitmap);
            }
            super.handleMessage(msg);
        }
    };


    static byte result[] = new byte[8192];

    // byte[]转换成Bitmap
    public static Bitmap Bytes2Bitmap(byte[] b) {
        try {

            if (b.length != 0) {
                return BitmapFactory.decodeByteArray(b, 0, b.length);
            }
        } catch (Exception e) {
            Message message = new Message();
            message.obj = "Bytes2Bitmap";
            uiHandler.sendMessage(message);
        }
        return null;
    }

    //private static int IMAGE_PORT = 6891;

/*
    public byte[] receive() throws SocketException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (datagramSocket == null) {
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(new InetSocketAddress(IMAGE_PORT));
        } else
            try {
                datagramSocket = new DatagramSocket(IMAGE_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        while (true) {
            datagramPacket = new DatagramPacket(b, b.length);
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(MainActivity3.this,"",Toast.LENGTH_SHORT).show();
            String msg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
            if (msg.startsWith(";!")) {
                System.out.println("-->接收到所有数据");
                break;
            }
            baos.write(datagramPacket.getData(), 0, datagramPacket.getLength());
            //System.out.println("-->正在接收数据:"+datagramPacket.getData());
        }
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();


    }*/

}
