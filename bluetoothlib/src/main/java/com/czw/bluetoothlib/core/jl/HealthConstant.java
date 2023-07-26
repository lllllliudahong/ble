package com.czw.bluetoothlib.core.jl;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;

/**
 * 健康APP常量
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public class HealthConstant {

    public final static String WATCH_TAG = "warm_watch";
    public final static int REQUEST_CODE_PERMISSIONS = 2333;
    public final static int REQUEST_CODE_CHECK_GPS = 2334;
    public final static int REQUEST_CODE_CAMERA = 2335;
    public final static int REQUEST_CODE_STORAGE = 2336;

    public final static int DEFAULT_CONNECT_WAY = BluetoothConstant.PROTOCOL_TYPE_BLE;
    public final static boolean ONLY_CONNECT_BLE = true; //是否仅连接BLE

    public final static String DIR_UPDATE = "upgrade";
    public final static String DIR_WATCH = "watch";
    public final static String DIR_USER = "user";
    public final static String DIR_NFC = "nfc";
    //Watch configure
    public final static int WATCH_MAX_COUNT = 30; //最多表盘数
    //是否使用测试服务器
    public final static boolean USE_TEST_SERVER = false;
    //测试功能
    public final static boolean TEST_DEVICE_FUNCTION = false;
    //同步设备电量
    public final static boolean SYNC_DEV_POWER = false;
    //测试NFC功能
    public final static boolean TEST_NFC_FUNCTION = false;

    //default package name
    public final static String PACKAGE_NAME_SYS_MESSAGE = "com.android.mms";
    public final static String PACKAGE_NAME_WECHAT = "com.tencent.mm";
    public final static String PACKAGE_NAME_QQ = "com.tencent.mobileqq";
    public final static String PACKAGE_NAME_DING_DING = "com.alibaba.android.rimet";

    public final static String PACKAGE_WHATSAPP = "com.whatsapp";
    public final static String PACKAGE_FACEBOOK_KATANA = "com.facebook.katana";
    public final static String PACKAGE_TWITTER_ANDROID = "com.twitter.android";

}

