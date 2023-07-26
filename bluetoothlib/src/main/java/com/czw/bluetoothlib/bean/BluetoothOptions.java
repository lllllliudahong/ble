package com.czw.bluetoothlib.bean;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

/**
 * @author chenxiaojin
 * @date 2020/7/18
 * @description 蓝牙配置选项:包括连接超时、重试次数
 */
public class BluetoothOptions implements Parcelable {
    // 设备类型
    private int deviceModel;
    // 设备mac地址
    private String deviceMac;


    protected BluetoothOptions(Parcel in) {
        this.deviceModel = in.readInt();
        this.deviceMac = in.readString();
    }

    public static final Creator<BluetoothOptions> CREATOR = new Creator<BluetoothOptions>() {
        @Override
        public BluetoothOptions createFromParcel(Parcel in) {
            return new BluetoothOptions(in);
        }

        @Override
        public BluetoothOptions[] newArray(int size) {
            return new BluetoothOptions[size];
        }
    };



    public BluetoothOptions(Builder builder) {
        Log.e("BluetoothLEDevice", "BluetoothOptions  设备: "+builder.deviceMac );
        this.deviceModel = builder.deviceModel;
        this.deviceMac = builder.deviceMac;


    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(deviceModel);
        dest.writeString(deviceMac);


    }

    public int getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceMac() {
        return deviceMac;
    }




    /**
     * BluetoothOptions构造器
     */
    public static class Builder {
        // 设备类型
        private int deviceModel;
        // 设备mac地址
        private String deviceMac;

        public Builder setDeviceModel(int deviceModel) {
            this.deviceModel = deviceModel;
            return this;
        }
        public Builder setDeviceMac(String deviceMac) {
            this.deviceMac = deviceMac;
            return this;
        }
        public BluetoothOptions build() {
            return new BluetoothOptions(this);
        }
    }
}
