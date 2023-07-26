package com.czw.bluetoothlib.util;


public class SPUtils {

    static SPUtils util;

    public static SPUtils getInstance() {
        if (util == null) {
            util = new SPUtils();
            SPCacheUtils.getInstance();
        }
        return util;
    }

    public static String getBtMac() {
        return (String) SPCacheUtils.getData("btMac", "");
    }

    public static void setBtMac(String btMac) {
        SPCacheUtils.putData("btMac", btMac);
    }

    public static String getBLEMac() {
        return (String) SPCacheUtils.getData("bleMac", "");
    }

    public static void setBLEMac(String btMac) {
        SPCacheUtils.putData("bleMac", btMac);
    }


    public static int getDeviceMode() {
        return (int) SPCacheUtils.getData("deviceMode", 0);
    }

    public static void setDeviceMode(int deviceMode) {
        SPCacheUtils.putData("deviceMode", deviceMode);
    }


    public static String getDevicePlate() {
        return (String) SPCacheUtils.getData("device_Plate_1", "00");
    }

    public static void setDevicePlate(String deviceMode) {
        SPCacheUtils.putData("device_Plate_1", deviceMode);
    }


    public static String getDevicePlateFirmware() {
        return (String) SPCacheUtils.getData("device_Plate_Firmware", "00");
    }

    public static void setDevicePlateFirmware(String deviceMode) {
        SPCacheUtils.putData("device_Plate_Firmware", deviceMode);

    }


    public static String getDevicePlateFirmwareCustomerBranch() {
        return (String) SPCacheUtils.getData("device_Plate_Firmware_customerBranch", "00");
    }

    public static void setDevicePlateFirmwareCustomerBranch(String key) {
        SPCacheUtils.putData("device_Plate_Firmware_customerBranch", key);

    }


    public static boolean getDeviceISOTA() {
        return (boolean) SPCacheUtils.getData("deviceISOTA", false);
    }

    public static void setDeviceISOTA(boolean deviceMode) {
        SPCacheUtils.putData("deviceISOTA", deviceMode);
    }

    public static boolean getISOTAPage() {
        return (boolean) SPCacheUtils.getData("ISOTAPage", false);
    }

    public static void setISOTAPage(boolean flag) {
        SPCacheUtils.putData("ISOTAPage", flag);
    }


    public static int getBattery() {
        return (int) SPCacheUtils.getData("batteryLevel", 100);
    }

    public static void setBattery(int val) {
        SPCacheUtils.putData("batteryLevel", val);
    }


    public static String getDeviceUUID() {
        return (String) SPCacheUtils.getData("DeviceUUID", "");
    }

    public static void setDeviceUUID(String flag) {
        SPCacheUtils.putData("DeviceUUID", flag);
    }


    public static boolean getAutoSyncData() {
        return (boolean) SPCacheUtils.getData("AutoSyncData", true);
    }

    public static void setAutoSyncData(boolean val) {
        SPCacheUtils.putData("AutoSyncData", val);
    }


    public static boolean getOnAlipayPage() {
        return (boolean) SPCacheUtils.getData("ON_ALIPAY_PAGE", false);
    }

    public static void setOnAlipayPage(boolean val) {
        SPCacheUtils.putData("ON_ALIPAY_PAGE", val);
    }


}
