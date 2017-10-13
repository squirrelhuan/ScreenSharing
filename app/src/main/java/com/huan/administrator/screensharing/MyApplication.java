package com.huan.administrator.screensharing;

import android.app.Application;

import com.huan.mylog.MyLog;

/**
 * Created by Administrator on 2017/5/22.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        MyLog myLog = new MyLog(this);
        myLog.initialization();
        myLog.setPrintType(MyLog.PrintType.All);// 设置打印类型
        myLog.setErrorToast("对不起程序崩溃了");// 设置崩溃提示
        // mylog.setErrorCatchedListener(new one);

        super.onCreate();
    }
}
