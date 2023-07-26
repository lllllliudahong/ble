package com.czw.bluetoothlib.core;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.zwzn.fitble.BleManager;
import com.zwzn.fitble.callback.BleScanCallback;
import com.zwzn.fitble.data.BleDevice;
import com.zwzn.fitble.scan.BleScanRuleConfig;
import com.zwzn.fitble.utils.HexUtil;
import com.czw.bluetoothlib.listener.BluetoothScanListener;

import java.util.List;

/**
 * @author chenxiaojin
 * @date 2020/7/22
 * @description 蓝牙搜索器
 */
public class BluetoothScanner {
    private static final String TAG = "BluetoothScanner";
    private BluetoothScanListener scanListener;

    //只能通过APP自己来管理
    Context mContext;
    byte[] scanRecord = null;
    int rssi = 0;
    boolean isOTA = false;
    BluetoothDevice device = null;

    public BluetoothScanner(Context context) {
        this.mContext = context;
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();

        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    public void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                if (null != scanListener) {
                    scanListener.onStartScan();
                }
                Log.e(TAG, "Bluetooth is. onScanStarted");
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
//                Log.e("liuhong", "Bluetooth is. onScanning:" + bleDevice.getDevice().getName() + "--mac:" + bleDevice.getMac() + " -- " + HexUtil.formatHexString(bleDevice.getScanRecord()));
                scanRecord = bleDevice.getScanRecord();
                device = bleDevice.getDevice();
                rssi = bleDevice.getRssi();

                if (scanRecord != null) {
                    if (scanRecord.length > 14) {
                        String s = HexUtil.formatHexString(scanRecord).toLowerCase();
//                        Log.e(TAG, "Bluetooth is. onScanning:" + s );
                        if (s.contains("1effd605")) {//杰理

                            processDeviceFounded(device, rssi, scanRecord, 6);
                        } else if (scanRecord[1] == (byte) 0xff && scanRecord[2] == (byte) 0xd6 && scanRecord[3] == (byte) 0x05) {//杰理

                            processDeviceFounded(device, rssi, scanRecord, 6);
                        } else if (scanRecord[1] == (byte) 0xff && scanRecord[2] == (byte) 0x18 && scanRecord[3] == (byte) 0x00) {//杰理

                            processDeviceFounded(device, rssi, scanRecord, 6);
                        } else if (scanRecord[4] == (byte) 0xff && scanRecord[5] == (byte) 0x17 && scanRecord[6] == (byte) 0x00) {//中科
                            processDeviceFounded(device, rssi, scanRecord, 7);
                        } else if (scanRecord[4] == (byte) 0xff && scanRecord[5] == (byte) 0x16 && scanRecord[6] == (byte) 0x00) {//中科
                            processDeviceFounded(device, rssi, scanRecord, 7);
                        } else if (scanRecord[12] == (byte) 0xff && scanRecord[13] == (byte) 0x16) {//
                            processDeviceFounded(device, rssi, scanRecord, 7);
                        }
                    }
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Log.e(TAG, "Bluetooth is. onScanFinished");
                stopScan();
            }
        });
    }




    public void stopScan() {
        if (null != scanListener) {
            scanListener.onStopScan();
        }

    }

    private void processDeviceFounded(BluetoothDevice device, int rssi, byte[] scanRecord, int deviceMode) {
        if (null != scanListener) {
            scanListener.onDeviceFounded(device, rssi, scanRecord, deviceMode);
        }
    }

    public void setBluetoothScanListener(BluetoothScanListener scanListener) {
        this.scanListener = scanListener;
    }

}
