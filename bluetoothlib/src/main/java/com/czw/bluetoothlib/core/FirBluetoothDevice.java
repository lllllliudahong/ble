package com.czw.bluetoothlib.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.util.Log;

import com.czw.bluetoothlib.bean.BluetoothDeviceData;
import com.czw.bluetoothlib.core.jl.BluetoothEventListener;
import com.czw.bluetoothlib.listener.BluetoothDeviceDataCallback;
import com.czw.bluetoothlib.listener.BluetoothDeviceStateListener;
import com.czw.bluetoothlib.util.ByteUtil;
import com.czw.bluetoothlib.util.SPUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FirBluetoothDevice extends BluetoothEventListener {
    String TAG = "FirBluetoothDevice";
    public int deviceMode = 0;//默认设备为普通设备
    protected List<BluetoothDeviceStateListener> deviceStateListeners = new ArrayList<>();
    protected List<BluetoothDeviceDataCallback> deviceDataCallbacks = new ArrayList<>();
    protected BluetoothAdapter bluetoothAdapter;
    protected BluetoothDevice bluetoothDevice;


    public int getDeviceMode() {
        return SPUtils.getDeviceMode();
    }

    public void setDeviceMode(int deviceMode) {
        this.deviceMode = deviceMode;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public void addDeviceDataCallback(BluetoothDeviceDataCallback dataCallback) {
        deviceDataCallbacks.add(dataCallback);
    }

    protected void notifyDataReadError(String deviceMac, String serviceUUID,
                                       String characteristicUUID, String errorMessage) {
        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onReadError(deviceMac, serviceUUID, characteristicUUID, errorMessage);
        }
    }


    protected void notifyDeviceDataRead(BluetoothDeviceData deviceData, int state) {
        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onDataRead(deviceData, state);
        }
    }

    protected void notifyDeviceDataWrite(BluetoothDeviceData deviceData, int state) {
        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onDataWrite(deviceData, state);
        }
    }

    protected void notifyDeviceDataChanged(BluetoothDeviceData deviceData) {
        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onDataChanged(deviceData);

        }
    }

    public void removeDeviceDataCallback(BluetoothDeviceDataCallback dataCallback) {
//        deviceDataCallbacks.remove(dataCallback);
        Iterator<BluetoothDeviceDataCallback> iterator = deviceDataCallbacks.iterator();
        while (iterator.hasNext()) {
            if (dataCallback == iterator.next()) {
                iterator.remove();
                break;
            }
        }
    }

    public void addDeviceStateListener(BluetoothDeviceStateListener deviceStateListener) {
        if (!deviceStateListeners.contains(deviceStateListener)) {
            deviceStateListeners.add(deviceStateListener);
        }
    }

    public void removeDeviceStateListener(BluetoothDeviceStateListener deviceStateListener) {
        Iterator<BluetoothDeviceStateListener> iterator = deviceStateListeners.iterator();
        while (iterator.hasNext()) {
            if (deviceStateListener == iterator.next()) {
                iterator.remove();
                break;
            }
        }
    }


    //  41:42:E4:08:BD:A1
    protected void notifyBTConnectStateChange(String deviceMac, boolean isBonded) {
        byte[] bytes = new byte[9];
        bytes[0] = (byte) 0x54;
        bytes[1] = (byte) (isBonded == true ? 0x01 : 0x02);

//        ByteUtil.hexStringToBytes()
//        bytes[2] = (byte) deviceMac.substring(0,2);
//        bytes[3] = (byte) (isBonded == true ? 0x01 : 0x02);
//        bytes[4] = (byte) (isBonded == true ? 0x01 : 0x02);
//        bytes[5] = (byte) (isBonded == true ? 0x01 : 0x02);
//        bytes[6] = (byte) (isBonded == true ? 0x01 : 0x02);
//        bytes[7] = (byte) (isBonded == true ? 0x01 : 0x02);
//        bytes[8] = (byte) (isBonded == true ? 0x01 : 0x02);
//        sendData(bytes);

    }


    protected void notifyConnectStateChange(BluetoothDevice device, int oldState, int newState) {

        Log.e("BluetoothLEDevice", TAG + "  notifyConnectStateChange --10086-- oldState:" + oldState + ",mac:" + device.getAddress() + ",newState:" + newState);
        for (BluetoothDeviceStateListener stateListener : deviceStateListeners) {
            stateListener.onStateChange(device, oldState, newState);
        }
    }

    protected void notifyDeviceReady(String deviceMac) {
        for (BluetoothDeviceStateListener stateListener : deviceStateListeners) {
            stateListener.onReady(deviceMac);
        }
    }

    protected void notifyDiscoverServicesError(String deviceMac, String errorMessage) {
        for (BluetoothDeviceStateListener stateListener : deviceStateListeners) {
            stateListener.onDiscoverServicesError(deviceMac, errorMessage);
        }
    }

    protected void notifyDeviceConnectTimeout(String deviceMac) {
        synchronized (deviceStateListeners) {
            for (BluetoothDeviceStateListener stateListener : deviceStateListeners) {
                stateListener.onConnectTimeout(deviceMac);
            }
        }
    }

    public void connect() {
        if (null == bluetoothAdapter) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (null == bluetoothAdapter) {
            try {
                throw new RuntimeException("Not support bluetooth.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
        firDeviceConnect(1);
    }

    public void disconnect() {
        firDeviceConnect(0);
        disFirDeviceConnect();
    }

    public boolean isConnected() {
//        Log.e(TAG, "isConnected   deviceMode: " + SPUtils.getDeviceMode());
        return isFirDeviceConnected();
    }

    public void close() {
        firDeviceClose();
    }

    public void sendData(byte[] data) {

//        Log.e(TAG, "Device is connected.发送数据 : --deviceMode: " + deviceMode + " ---: " + SPUtils.getDeviceMode() + " ---bytesToHex: " + ByteUtil.bytesToHex(data));
        Log.e(TAG, "Device is connected.发送数据 : --deviceMode: " + " ---bytesToHex: " + ByteUtil.bytesToHex(data));

        //  发送数据
        sendFirDeviceData(data);
    }

    protected void sendFirDeviceData(byte[] data) {

    }


    protected void sendJlDeviceData(byte[] data) {

    }

    protected void disJlDeviceConnect() {


    }

    protected boolean isJlDeviceConnected() {
        return false;
    }

    protected void jlDeviceClose() {
    }

    protected void firDeviceClose() {
    }

    protected void disFirDeviceConnect() {
    }

    protected boolean isFirDeviceConnected() {
        return false;
    }

    protected boolean firDeviceConnect(int type) {
        return false;
    }

    protected boolean jlDeviceConnect() {
        return false;
    }
}
