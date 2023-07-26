package com.czw.bluetoothlib.listener;

import android.bluetooth.BluetoothDevice;

import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;

/**
 * @author chenxiaojin
 * @date 2020/7/22
 * @description
 */
public interface BluetoothScanListener {

    void onStartScan();

    void onScanFailed(String errorMessage);

    void onDeviceFounded(BluetoothDevice device, int rssi, byte[] scanRecord,int deviceMode);

    void onStopScan();
}
