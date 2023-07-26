package com.czw.bluetoothlib.core.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanSettings.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jieli.bluetooth_connect.bean.BluetoothOption;
import com.jieli.bluetooth_connect.bean.ErrorInfo;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.interfaces.IBluetoothDiscovery;
import com.jieli.bluetooth_connect.interfaces.listener.OnBtDiscoveryListener;
import com.jieli.bluetooth_connect.tool.BtDiscoveryCbManager;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.bluetooth_connect.util.ParseDataUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 从杰理demo中获取，用于蓝牙搜索
 */
public class BluetoothDiscovery implements IBluetoothDiscovery {
    private static final String TAG = "BluetoothDiscovery";
    private final List<BluetoothDevice> mDiscoveredDevices = new ArrayList();
    private final List<BluetoothDevice> mDiscoveredEdrDevices = new ArrayList();
    private final Context mContext;
    private final BtDiscoveryCbManager mBtDiscoveryCbManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDiscovery.BluetoothReceiver mBluetoothReceiver;
    private BluetoothDiscovery.BluetoothDiscoveryReceiver mBluetoothDiscoveryReceiver;
    private BluetoothLeScanner mBluetoothLeScanner;
    private final BluetoothOption mBluetoothOption;
    private int mScanType;
    private volatile boolean mIsBleScanning = false;
    private long mTimeout = 8000L;
    private static final int MSG_DISCOVERY_BLE_TIMEOUT = 1011;
    private static final int MSG_DISCOVERY_EDR_TIMEOUT = 1022;
    private final Handler mHandler = new Handler(Looper.getMainLooper(), (msg) -> {
        switch (msg.what) {
            case 1011:
                if (this.mIsBleScanning) {
                    JL_Log.w(TAG, "-MSG_DISCOVERY_BLE_TIMEOUT- stopBLEScan: ");
                    this.stopBLEScan();
                }
                break;
            case 1022:
                if (this.mBluetoothAdapter.isDiscovering()) {
                    JL_Log.w(TAG, "-MSG_DISCOVERY_EDR_TIMEOUT- stopDeviceScan: ");
                    this.stopDeviceScan();
                }
        }

        return false;
    });
    private final LeScanCallback leScanCallback = (device, rssi, scanRecord) -> {
        this.filterDevice(device, rssi, scanRecord, true);
    };
    @RequiresApi(21)
    private final ScanCallback mScanCallback = new ScanCallback() {
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e(TAG,"onBatchScanResults");
        }

        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.e(TAG,"onScanResult");
            if (result != null && result.getScanRecord() != null) {
                BluetoothDevice device = result.getDevice();
                boolean isBleEnableConnect = true;
                if (VERSION.SDK_INT >= 26) {
                    isBleEnableConnect = result.isConnectable();
                }
                Log.e(TAG,"onScanResult:"+device.getAddress());
                BluetoothDiscovery.this.filterDevice(device, result.getRssi(), result.getScanRecord().getBytes(), isBleEnableConnect);
            }

        }

        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            BluetoothDiscovery.this.mBtDiscoveryCbManager.onDiscoveryError(new ErrorInfo(errorCode, "scan ble error."));
        }
    };

    public BluetoothDiscovery(Context context, BluetoothOption option, OnBtDiscoveryListener listener) {
        this.mContext = (Context) ConnectUtil.checkNotNull(context);
        if (option == null) {
            option = BluetoothOption.createDefaultOption();
        }

        this.mBluetoothOption = option;
        this.mBtDiscoveryCbManager = new BtDiscoveryCbManager();
        this.addListener(listener);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.getBluetoothLeScanner();
        this.registerBtReceiver();
    }

    public void addListener(OnBtDiscoveryListener listener) {
        this.mBtDiscoveryCbManager.addListener(listener);
    }

    public void removeListener(OnBtDiscoveryListener listener) {
        this.mBtDiscoveryCbManager.removeListener(listener);
    }

    public void setBluetoothOption(BluetoothOption option) {
    }

    public ArrayList<BluetoothDevice> getDiscoveredBluetoothDevices() {
        return this.mScanType == 1 ? new ArrayList(this.mDiscoveredEdrDevices) : new ArrayList(this.mDiscoveredDevices);
    }

    public boolean isBleScanning() {
        return this.mIsBleScanning;
    }

    public boolean isDeviceScanning() {
        return this.mBluetoothAdapter != null && this.mBluetoothAdapter.isDiscovering();
    }

    public boolean isScanning() {
        return this.isDeviceScanning() || this.mIsBleScanning;
    }

    public int getScanType() {
        return this.mScanType;
    }

    public boolean startDeviceScan(long timeout) {
        return this.startDeviceScan(1, timeout);
    }

    public boolean startDeviceScan(int type, long timeout) {
        this.mScanType = type;
        if (type == 0) {
            return this.startBLEScan(timeout);
        } else {
            if (this.mBluetoothAdapter == null) {
                this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }

            if (this.mBluetoothAdapter == null) {
                JL_Log.e(TAG, "this device is not supported bluetooth.");
                return false;
            } else if (!BluetoothUtil.isBluetoothEnable()) {
                return false;
            } else {
                if (this.mIsBleScanning) {
                    JL_Log.w(TAG, "-startDeviceScan- stopBLEScan: ");
                    this.stopBLEScan();
                }

                if (timeout <= 0L) {
                    this.mTimeout = 8000L;
                } else {
                    this.mTimeout = timeout;
                }

                if (this.mBluetoothAdapter.isDiscovering()) {
                    boolean ret = this.stopDeviceScan();
                    if (!ret) {
                        return false;
                    }

                    int count = 0;

                    while (this.mBluetoothAdapter.isDiscovering()) {
                        SystemClock.sleep(100L);
                        count += 100;
                        if (count > 2000) {
                            break;
                        }
                    }

                    this.mDiscoveredEdrDevices.clear();
                }

                this.registerReceiver();
                boolean bRet = this.mBluetoothAdapter.startDiscovery();
                JL_Log.i(TAG, "-startDiscovery- >>>>>> bRet : " + bRet);
                if (!bRet) {
                    this.notifyDiscoveryStatus(false, false);
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    public boolean stopDeviceScan() {
        if (this.mBluetoothAdapter == null) {
            JL_Log.e(TAG, "stopDeviceScan :: this device is not supported bluetooth.");
            return false;
        } else if (!this.mBluetoothAdapter.isDiscovering()) {
            return true;
        } else if (!this.mBluetoothAdapter.isEnabled()) {
            this.unregisterReceiver();
            return true;
        } else {
            boolean bRet = this.mBluetoothAdapter.cancelDiscovery();
            JL_Log.w(TAG, "-cancelDiscovery- >>>>>> bRet = " + bRet);
            if (!bRet) {
                return false;
            } else {
                this.mHandler.removeMessages(1022);
                return true;
            }
        }
    }

    public boolean startBLEScan(long timeout) {
        if (this.mBluetoothAdapter == null) {
            this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (this.mBluetoothAdapter == null) {
            JL_Log.e(TAG, "startBLEScan :: this device is not supported bluetooth.");
            return false;
        } else {
            this.mScanType = 0;
            if (!BluetoothUtil.isBluetoothEnable()) {
                this.notifyDiscoveryStatus(true, false);
                return false;
            } else {
                if (timeout <= 0L) {
                    timeout = 8000L;
                }

                if (this.mIsBleScanning) {
                    JL_Log.i(TAG, "scanning ble ..... timeout = " + timeout);
                    if (this.mBluetoothLeScanner != null) {
                        this.mBluetoothLeScanner.flushPendingScanResults(this.mScanCallback);
                    }

                    this.mDiscoveredDevices.clear();
                    this.mHandler.removeMessages(1011);
                    this.mHandler.sendEmptyMessageDelayed(1011, timeout);
                    this.notifyDiscoveryStatus(true, true);
                    return true;
                } else {
                    if (this.isDeviceScanning()) {
                        this.stopDeviceScan();
                    }

                    this.mHandler.removeMessages(1011);
                    this.mHandler.sendEmptyMessageDelayed(1011, timeout);
                    this.mIsBleScanning = true;
                    this.notifyDiscoveryStatus(true, true);
                    if (!ConnectUtil.isHasLocationPermission(this.mContext)) {
                        this.mIsBleScanning = false;
                        this.notifyDiscoveryStatus(true, false);
                        return false;
                    } else {
                        if (VERSION.SDK_INT >= 21 && this.getBluetoothLeScanner() != null) {
                            ScanSettings scanSettings;
                            if (VERSION.SDK_INT >= 23) {
                                scanSettings = (new Builder()).setScanMode(this.mBluetoothOption.getBleScanMode()).setMatchMode(1).build();
                            } else {
                                scanSettings = (new Builder()).setScanMode(this.mBluetoothOption.getBleScanMode()).build();
                            }

                            List<ScanFilter> filterList = new ArrayList();
                            this.mBluetoothLeScanner.startScan(/*filterList, scanSettings, */this.mScanCallback);
                            JL_Log.i(TAG, "-startBLEScan- >>>>>> startScan :>> ");
                        } else {
                            boolean bRet = this.mBluetoothAdapter.startLeScan(this.leScanCallback);
                            JL_Log.i(TAG, "-startBLEScan- >>>>>> bRet : " + bRet);
                            if (!bRet) {
                                this.notifyDiscoveryStatus(true, false);
                                return false;
                            }
                        }

                        this.mDiscoveredDevices.clear();
                        return true;
                    }
                }
            }
        }
    }

    public boolean stopBLEScan() {
        if (this.mBluetoothAdapter == null) {
            JL_Log.e(TAG, "stopBLEScan :: this device is not supported bluetooth.");
            return false;
        } else if (!this.mIsBleScanning) {
            return true;
        } else {
            this.stopBleScanNoCallback();
            this.notifyDiscoveryStatus(true, false);
            return true;
        }
    }

    public void destroy() {
        this.mBtDiscoveryCbManager.destroy();
        this.mHandler.removeCallbacksAndMessages((Object) null);
        this.stopDeviceScan();
        this.stopBLEScan();
        this.unregisterReceiver();
        this.unregisterBtReceiver();
    }

    private BluetoothLeScanner getBluetoothLeScanner() {
        if (VERSION.SDK_INT >= 21 && this.mBluetoothAdapter != null && this.mBluetoothLeScanner == null) {
            this.mBluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
        }

        return this.mBluetoothLeScanner;
    }

    private void registerBtReceiver() {
        if (null == this.mBluetoothReceiver) {
            this.mBluetoothReceiver = new BluetoothDiscovery.BluetoothReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            this.mContext.registerReceiver(this.mBluetoothReceiver, intentFilter);
        }

    }

    private void unregisterBtReceiver() {
        if (null != this.mBluetoothReceiver) {
            this.mContext.unregisterReceiver(this.mBluetoothReceiver);
            this.mBluetoothReceiver = null;
        }

    }

    private void registerReceiver() {
        if (null == this.mBluetoothDiscoveryReceiver) {
            this.mBluetoothDiscoveryReceiver = new BluetoothDiscovery.BluetoothDiscoveryReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED");
            intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
            intentFilter.addAction("android.bluetooth.device.action.FOUND");
            this.mContext.registerReceiver(this.mBluetoothDiscoveryReceiver, intentFilter);
        }

    }

    private void unregisterReceiver() {
        if (this.mBluetoothDiscoveryReceiver != null) {
            this.mContext.unregisterReceiver(this.mBluetoothDiscoveryReceiver);
            this.mBluetoothDiscoveryReceiver = null;
        }

    }

    private void notifyDiscoveryStatus(boolean bBle, boolean bStart) {
        JL_Log.i(TAG, "-notifyDiscoveryStatus- bBle : " + bBle + " ,bStart : " + bStart);
        if (this.mScanType == 0 && bBle) {
            this.mBtDiscoveryCbManager.onDiscoveryStatusChange(true, bStart);
        } else if (this.mScanType == 1 && !bBle) {
            this.mBtDiscoveryCbManager.onDiscoveryStatusChange(false, bStart);
        }

        if (!bStart) {
            this.mIsBleScanning = false;
            this.mScanType = 0;
        }

    }

    private void stopBleScanNoCallback() {
        if (this.mIsBleScanning) {
            JL_Log.i(TAG, "-stopBLEScan- >>>>>> ");
            this.mIsBleScanning = false;
            if (!BluetoothUtil.isBluetoothEnable()) {
                return;
            }

            if (VERSION.SDK_INT >= 21 && this.getBluetoothLeScanner() != null) {
                try {
                    this.mBluetoothLeScanner.stopScan(this.mScanCallback);
                } catch (Exception var3) {
                    var3.printStackTrace();
                }
            } else if (this.mBluetoothAdapter != null) {
                try {
                    this.mBluetoothAdapter.stopLeScan(this.leScanCallback);
                } catch (Exception var2) {
                    var2.printStackTrace();
                }
            }
        }

        this.mHandler.removeMessages(1011);
    }

    private void filterDevice(BluetoothDevice device, int rssi, byte[] scanRecord, boolean isBleEnableConnect) {
        if (device != null) {
            Log.e(TAG,"filterDevice:"+device.getAddress());
            if (this.mBluetoothOption.getBleScanStrategy() == 0) {
                if (BluetoothUtil.isBluetoothEnable() && !TextUtils.isEmpty(device.getName()) && !this.mDiscoveredDevices.contains(device)) {
                    this.mDiscoveredDevices.add(device);
                    this.mBtDiscoveryCbManager.onDiscoveryDevice(device, (new BleScanMessage()).setRawData(scanRecord).setRssi(rssi).setEnableConnect(isBleEnableConnect));
                }
            } else {
                BleScanMessage bleScanMessage = ParseDataUtil.isFilterBleDevice(this.mBluetoothOption, scanRecord);
                if (null == bleScanMessage)
                    bleScanMessage = new BleScanMessage();
                if (bleScanMessage != null && !TextUtils.isEmpty(device.getName()) && BluetoothUtil.isBluetoothEnable()) {
                    if (bleScanMessage.isEnableConnect() && !isBleEnableConnect) {
                        bleScanMessage.setEnableConnect(false);
                    }

                    bleScanMessage.setRawData(scanRecord).setRssi(rssi);
                    if (bleScanMessage.isShowDialog()) {
                        this.mBtDiscoveryCbManager.onShowProductDialog(device, bleScanMessage);
                    }

                    if (!this.mDiscoveredDevices.contains(device)) {
                        this.mDiscoveredDevices.add(device);
                        this.mBtDiscoveryCbManager.onDiscoveryDevice(device, bleScanMessage);
                    }
                }
            }
        }

    }

    private class BluetoothReceiver extends BroadcastReceiver {
        private BluetoothReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 0);
                    if (state != 0 && state == 10) {
                        BluetoothDiscovery.this.mHandler.removeCallbacksAndMessages((Object) null);
                        BluetoothDiscovery.this.mIsBleScanning = false;
                        BluetoothDiscovery.this.mDiscoveredDevices.clear();
                        BluetoothDiscovery.this.mDiscoveredEdrDevices.clear();
                        BluetoothDiscovery.this.unregisterReceiver();
                    }
                }

            }
        }
    }

    private class BluetoothDiscoveryReceiver extends BroadcastReceiver {
        private BluetoothDiscoveryReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {
                    String var4 = (String) Objects.requireNonNull(action);
                    byte var5 = -1;
                    switch (var4.hashCode()) {
                        case -1780914469:
                            if (var4.equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
                                var5 = 1;
                            }
                            break;
                        case 6759640:
                            if (var4.equals("android.bluetooth.adapter.action.DISCOVERY_STARTED")) {
                                var5 = 0;
                            }
                            break;
                        case 1167529923:
                            if (var4.equals("android.bluetooth.device.action.FOUND")) {
                                var5 = 2;
                            }
                    }

                    switch (var5) {
                        case 0:
                            BluetoothDiscovery.this.mDiscoveredEdrDevices.clear();
                            BluetoothDiscovery.this.notifyDiscoveryStatus(false, true);
                            BluetoothDiscovery.this.mHandler.removeMessages(1022);
                            BluetoothDiscovery.this.mHandler.sendEmptyMessageDelayed(1022, BluetoothDiscovery.this.mTimeout);
                            break;
                        case 1:
                            BluetoothDiscovery.this.notifyDiscoveryStatus(false, false);
                            BluetoothDiscovery.this.mHandler.removeMessages(1022);
                            BluetoothDiscovery.this.unregisterReceiver();
                            break;
                        case 2:
                            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                            short rssi = intent.getShortExtra("android.bluetooth.device.extra.RSSI", (short) -1);
                            if (device == null || !BluetoothUtil.isBluetoothEnable()) {
                                return;
                            }

                            boolean foundRet = false;
                            int devType = device.getType();
                            switch (BluetoothDiscovery.this.mScanType) {
                                case 0:
                                    foundRet = 2 == devType || 3 == devType;
                                    break;
                                case 1:
                                    foundRet = 1 == device.getType();
                                    break;
                                case 2:
                                    foundRet = true;
                            }

                            if (foundRet && !BluetoothDiscovery.this.mDiscoveredEdrDevices.contains(device)) {
                                BluetoothDiscovery.this.mDiscoveredEdrDevices.add(device);
                                BluetoothDiscovery.this.mBtDiscoveryCbManager.onDiscoveryDevice(device, (new BleScanMessage()).setEnableConnect(true).setRssi(rssi));
                            }
                    }

                }
            }
        }
    }
}

