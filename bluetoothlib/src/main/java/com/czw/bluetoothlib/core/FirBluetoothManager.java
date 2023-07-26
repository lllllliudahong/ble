package com.czw.bluetoothlib.core;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.zwzn.fitble.BleManager;
import com.czw.bluetoothlib.bean.BluetoothDeviceData;
import com.czw.bluetoothlib.listener.BluetoothDeviceDataCallback;
import com.czw.bluetoothlib.listener.BluetoothDeviceStateListener;
import com.czw.bluetoothlib.util.ByteUtil;
import com.czw.bluetoothlib.util.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirBluetoothManager implements BluetoothDeviceStateListener, BluetoothDeviceDataCallback {
    private static final String TAG = "FirBluetoothManager";
    private static FirBluetoothManager FirBluetoothManager;

    private Map<String, BluetoothLEDevice> devicesMap = new HashMap<>();
    private List<BluetoothDeviceStateListener> deviceStateListeners = new ArrayList<>();
    private List<BluetoothDeviceDataCallback> deviceDataCallbacks = new ArrayList<>();

    public static FirBluetoothManager getInstance() {
        if (null == FirBluetoothManager) {
            FirBluetoothManager = new FirBluetoothManager();
        }
        return FirBluetoothManager;
    }

    /**
     * 连接设备, 通过此方法连接的设备才能被管理起来
     *
     * @param device
     * @return
     */
    public void connect(BluetoothLEDevice device) {
        if (device == null) {
            return;
        }
        devicesMap.clear();
        devicesMap.put(device.getBluetoothDevice().getAddress(), device);
        device.addDeviceStateListener(this);
//        device.addDeviceDataCallback(this);
        Log.e(TAG, "connect:" + devicesMap.size());
        device.connect();

    }


    public boolean isConnected(String deviceMac) {
        BluetoothLEDevice device = devicesMap.get(deviceMac);
        if (null == device) {
            Log.e(TAG, "Can not find device:" + deviceMac);
            return false;
        }
        return device.isConnected();
    }


    public BluetoothLEDevice getDevice(String deviceMac) {
        if (deviceMac == null || deviceMac.equals("")) {
            return null;
        }
        return devicesMap.get(deviceMac);
    }


    /**
     * 关闭设备
     *
     * @param deviceMac
     */
    public void close(String deviceMac) {
        BluetoothLEDevice device = devicesMap.get(deviceMac);
        if (null == device) {
            Log.e(TAG, "Can not find device, no need to close device, mac:" + deviceMac);
            return;
        }
//        device.close();
    }

    /**
     * 关闭当前管理的所有设备
     */
    public void closeAllDevices() {
        Log.e(TAG, "Close all devices.");
        for (BluetoothLEDevice device : devicesMap.values()) {
            close(device.getDeviceMac());
        }
    }

    /**
     * 移除设备, 移除后不再管理
     *
     * @param deviceMac
     */
    public void remove(String deviceMac) {
//        BluetoothLEDevice device = devicesMap.get(deviceMac);
//        if (null != device) {
//            close(deviceMac);
//            Log.e(TAG, "Remove device :" + deviceMac);
//            device.removeDeviceDataCallback(this);
//            device.removeDeviceStateListener(this);
//            devicesMap.remove(deviceMac);
//            device = null;
//            return;
//        }
        devicesMap.clear();
        removeAll();
        Log.e(TAG, "Can not find device, no need to remove. mac:" + deviceMac);
    }


    public void removeAll() {

        if (devicesMap.isEmpty()) {
            return;
        }
        for (int x = 0; x < devicesMap.size(); x++) {
            BluetoothLEDevice device = devicesMap.get(x);
            if (null != device) {
                close(device.getDeviceMac());
                Log.e(TAG, "Remove device :" + device.getDeviceMac());
                device.removeDeviceDataCallback(this);
                device.removeDeviceStateListener(this);
                devicesMap.remove(device.getDeviceMac());
                device = null;
                return;
            }
        }
    }


    public void bleIsConnected(String mac) {
        BleManager.getInstance().isConnected(mac);
    }

    public void disconnectAllDevice() {
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
        SPUtils.setBLEMac("00:00:00:00:00");

    }

    public void cancelScan() {
        BleManager.getInstance().cancelScan();
    }

    @Override
    public void onStateChange(BluetoothDevice device, int oldState, int newState) {
        notifyConnectStateChange(device, oldState, newState);
    }

    @Override
    public void onReady(String deviceMac) {
        notifyDeviceReady(deviceMac);
    }

    @Override
    public void onDiscoverServicesError(String deviceMac, String errorMessage) {
        notifyDiscoverServicesError(deviceMac, errorMessage);
    }

    @Override
    public void onConnectTimeout(String deviceMac) {
        notifyDeviceConnectTimeout(deviceMac);
    }

    @Override
    public void onDataRead(BluetoothDeviceData deviceData, int status) {
        notifyDeviceDataRead(deviceData, status);
    }

    @Override
    public void onDataWrite(BluetoothDeviceData deviceData, int status) {
        notifyDeviceDataWrite(deviceData, status);
    }

    @Override
    public void onDataChanged(BluetoothDeviceData deviceData) {

        Log.e(TAG, "Receive data ...onDataChanged:" + ByteUtil.bytesToHexSegment(deviceData.getData()));

//        notifyDeviceDataChanged(deviceData);
    }

    @Override
    public void onWriteError(String deviceMac, String serviceUUID, String characteristicUUID,
                             String errorMessage) {
        notifyDataWriteError(deviceMac, serviceUUID, characteristicUUID, errorMessage);
    }

    @Override
    public void onReadError(String deviceMac, String serviceUUID, String characteristicUUID,
                            String errorMessage) {
        notifyDataReadError(deviceMac, serviceUUID, characteristicUUID, errorMessage);
    }

    public void notifyDataReadError(String deviceMac, String serviceUUID,
                                    String characteristicUUID, String errorMessage) {
        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onReadError(deviceMac, serviceUUID, characteristicUUID, errorMessage);
        }
    }

    public void notifyConnectStateChange(BluetoothDevice device, int oldState, int newState) {
        for (BluetoothDeviceStateListener stateListener : deviceStateListeners) {
            stateListener.onStateChange(device, oldState, newState);
        }
    }

    public void notifyDeviceReady(String deviceMac) {
        for (BluetoothDeviceStateListener stateListener : deviceStateListeners) {
            stateListener.onReady(deviceMac);
        }
    }

    public void notifyDiscoverServicesError(String deviceMac, String errorMessage) {
        for (BluetoothDeviceStateListener stateListener : deviceStateListeners) {
            stateListener.onDiscoverServicesError(deviceMac, errorMessage);
        }
    }

    public void notifyDeviceConnectTimeout(String deviceMac) {
        for (BluetoothDeviceStateListener stateListener : deviceStateListeners) {
            stateListener.onConnectTimeout(deviceMac);
        }
    }

    public void notifyDeviceDataRead(BluetoothDeviceData deviceData, int state) {
        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onDataRead(deviceData, state);
        }
    }

    public void notifyDeviceDataWrite(BluetoothDeviceData deviceData, int state) {
        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onDataWrite(deviceData, state);
        }
    }

    public void notifyDeviceDataChanged(BluetoothDeviceData deviceData) {

        Log.e(TAG, "Receive data...notifyDeviceDataChanged--1 :" + ByteUtil.bytesToHexSegment(deviceData.getData()));

        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onDataChanged(deviceData);
            Log.e(TAG, "Receive data...notifyDeviceDataChanged--2 :" + ByteUtil.bytesToHexSegment(deviceData.getData()));
        }
    }

    public void notifyDataWriteError(String deviceMac, String serviceUUID,
                                     String characteristicUUID, String errorMessage) {
        for (BluetoothDeviceDataCallback dataCallback : deviceDataCallbacks) {
            dataCallback.onWriteError(deviceMac, serviceUUID, characteristicUUID, errorMessage);
        }
    }


    public void addDeviceStateListener(BluetoothDeviceStateListener deviceStateListener) {
        deviceStateListeners.add(deviceStateListener);
    }

    public void removeDeviceStateListener(BluetoothDeviceStateListener deviceStateListener) {
        deviceStateListeners.remove(deviceStateListener);
    }

    public void addDeviceDataCallback(BluetoothDeviceDataCallback dataCallback) {
        deviceDataCallbacks.add(dataCallback);
    }

    public void removeDeviceDataCallback(BluetoothDeviceDataCallback dataCallback) {
        deviceDataCallbacks.remove(dataCallback);
    }

    public void destroy() {
        for (BluetoothLEDevice device : devicesMap.values()) {
            device.removeDeviceDataCallback(this);
            device.removeDeviceStateListener(this);
        }
        devicesMap.clear();
        deviceDataCallbacks.clear();
        deviceStateListeners.clear();
    }
}

