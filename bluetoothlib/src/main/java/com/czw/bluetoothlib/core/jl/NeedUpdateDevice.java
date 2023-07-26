package com.czw.bluetoothlib.core.jl;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 需要更新的设备
 * @since 2022/6/29
 */
public class NeedUpdateDevice {
    private final String changeBleAddress;
    private final String originalBleAddress;
    private final int deviceType;
    private final int uid;
    private final int pid;

    public NeedUpdateDevice(String changeBleAddress, String originalBleAddress) {
        this(changeBleAddress, originalBleAddress, 0, 0, 0);
    }

    public NeedUpdateDevice(String changeBleAddress, String originalBleAddress, int deviceType, int uid, int pid) {
        this.changeBleAddress = changeBleAddress;
        this.originalBleAddress = originalBleAddress;
        this.deviceType = deviceType;
        this.uid = uid;
        this.pid = pid;
    }

    public String getChangeBleAddress() {
        return changeBleAddress;
    }

    public String getOriginalBleAddress() {
        return originalBleAddress;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public int getUid() {
        return uid;
    }

    public int getPid() {
        return pid;
    }

    @Override
    public String toString() {
        return "NeedUpdateDevice";
    }
}
