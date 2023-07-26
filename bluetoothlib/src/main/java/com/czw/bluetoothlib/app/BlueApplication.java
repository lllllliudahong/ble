package com.czw.bluetoothlib.app;

import android.app.Application;

import com.czw.bluetoothlib.util.SPUtils;
import com.zwzn.fitble.BleManager;


public class BlueApplication {
    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public static void setApplication(Application applica) {
        application = applica;
        SPUtils.getInstance();

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(false)
                .setReConnectCount(3, 3000) // 设置连接时重连次数和重连间隔（毫秒），默认为0次不重连
                .setConnectOverTime(10000) // 设置连接超时时间（毫秒），默认10秒
                .setOperateTimeout(8000);  // 配置操作超时
    }
}
