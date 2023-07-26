package com.czw.bluetoothlib.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.util.Log;

import com.czw.bluetoothlib.bean.BluetoothDeviceData;
import com.czw.bluetoothlib.core.jl.BluetoothHelper;
import com.czw.bluetoothlib.util.ByteUtil;
import com.czw.bluetoothlib.util.SPUtils;
import com.jieli.bluetooth_connect.bean.ErrorInfo;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.util.CHexConverter;

/**
 * 杰理设备处理
 */
public class JLBluetoothDevice extends FirBluetoothDevice {
    private static final String TAG = "JLBluetoothDevice";
    private BluetoothHelper mBluetoothHelper;
    private BluetoothGatt jlBluetoothGatt;


    private BluetoothHelper getBluetoothHelper() {
        if (null == mBluetoothHelper) {
            mBluetoothHelper = BluetoothHelper.getInstance();
        }
        return mBluetoothHelper;
    }

    public void addBluetoothEventListener() {
        getBluetoothHelper().addBluetoothEventListener(this);
    }

    public void removeBluetoothEventListener() {
        getBluetoothHelper().removeBluetoothEventListener(this);
    }


    @Override
    protected boolean jlDeviceConnect() {
        Log.e(TAG, "jlDeviceConnect()");
        SPUtils.setBLEMac(bluetoothDevice.getAddress());
        return getBluetoothHelper().connectDevice(bluetoothDevice);
    }

    @Override
    protected void jlDeviceClose() {
        Log.e(TAG, "jlDeviceClose()");
        if (null != bluetoothDevice) {
            getBluetoothHelper().disconnectDevice(bluetoothDevice);
        }
    }

    @Override
    protected boolean isJlDeviceConnected() {
        Log.e(TAG, "isJlDeviceConnected()..fun");
        if (null != bluetoothDevice) {
            return getBluetoothHelper().isConnectedBtDevice(bluetoothDevice);
        }
        return false;
    }


    @Override
    protected void disJlDeviceConnect() {
        if (null != bluetoothDevice) {
            getBluetoothHelper().disconnectDevice(bluetoothDevice);
        }
    }

    @Override
    protected void sendJlDeviceData(byte[] data) {
        // //  发送数据 -  杰理设备
        if (null != data) {
            Log.e(TAG, "sendJlDeviceData:" + CHexConverter.byte2HexStr(data));
        }
        //补充杰理设备UUID监听，
        if (null != getBluetoothHelper().getConnectedBtDevice()) {
            getBluetoothHelper().sendBleDataToDevice(getBluetoothHelper().getConnectedBtDevice(), data);
        }
    }


    @Override
    public void onAdapterStatus(boolean bEnabled) {
        super.onAdapterStatus(bEnabled);
        Log.e(TAG, "onAdapterStatus");
    }

    @Override
    public void onBtDiscoveryStatus(boolean bBle, boolean bStart) {
        super.onBtDiscoveryStatus(bBle, bStart);
        Log.e(TAG, "onBtDiscoveryStatus");
    }

    @Override
    public void onBtDiscovery(BluetoothDevice device, BleScanMessage bleScanMessage) {
        super.onBtDiscovery(device, bleScanMessage);
        Log.e(TAG, "onBtDiscovery");
    }

    @Override
    public void onShowDialog(BluetoothDevice device, BleScanMessage bleScanMessage) {
        super.onShowDialog(device, bleScanMessage);
        Log.e(TAG, "onShowDialog");
    }

    @Override
    public void onBleMtuChange(BluetoothGatt gatt, int mtu, int status) {
        super.onBleMtuChange(gatt, mtu, status);
        Log.e(TAG, "onBleMtuChange");
    }

    @Override
    public void onConnection(BluetoothDevice device, int status) {
        Log.e(TAG, "onConnection:" + status);
        notifyConnectStateChange(device, 0, status);
    }

    @Override
    public void onReceiveData(BluetoothDevice device, byte[] data) {

    }

    @Override
    public void onReceiveOldData(BluetoothDevice device, byte[] data) {
//        super.onReceiveOldData(device, data);
        //杰理设备数据返回
        if (null != data) {
        }

        Log.e(TAG, "Receive data...onReceiveOldData--1 :" + ByteUtil.bytesToHexSegment(data));

        // 数据变更回调通知
        notifyDeviceDataChanged(new BluetoothDeviceData(device.getAddress(), null, data));
    }

    @Override
    public void onSwitchConnectedDevice(BluetoothDevice device) {
        super.onSwitchConnectedDevice(device);
        Log.e(TAG, "onSwitchConnectedDevice");
    }

    @Override
    public void onHistoryRecord(int op, HistoryRecord record) {
        super.onHistoryRecord(op, record);
        Log.e(TAG, "onHistoryRecord");
    }

    @Override
    public void onError(ErrorInfo error) {
        super.onError(error);
        Log.e(TAG, "onError:" + error.toString());
    }


}
