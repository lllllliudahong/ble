package com.czw.bluetoothlib.core;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.czw.bluetoothlib.app.BlueApplication;
import com.czw.bluetoothlib.bean.BleConstantConfig;
import com.czw.bluetoothlib.bean.BluetoothDeviceData;
import com.czw.bluetoothlib.util.ByteUtil;
import com.czw.bluetoothlib.util.SPUtils;
import com.zwzn.fitble.BleManager;
import com.zwzn.fitble.callback.BleGattCallback;
import com.zwzn.fitble.callback.BleMtuChangedCallback;
import com.zwzn.fitble.callback.BleNotifyCallback;
import com.zwzn.fitble.callback.BleWriteCallback;
import com.zwzn.fitble.data.BleDevice;
import com.zwzn.fitble.exception.BleException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author chenxiaojin
 * @date 2020/7/18
 * @description 低功耗蓝牙设备, 包含设备所有功能
 * 注意不需要使用后需要调用destroy方法, 否则会有内存泄露问题
 */
public class BluetoothLEDevice extends JLBluetoothDevice {
    private static final String TAG = "BluetoothLEDevice";
    protected Context context;


    // 是否准备就绪, 准备就绪后才能发送消息
    private boolean isReady;
    BleDevice bleDevice;
    private boolean userBindDevice = false;

    public BluetoothLEDevice() {
        this.context = BlueApplication.getApplication();

        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultAdapter.getProfileProxy(this.context, mListener, BluetoothProfile.A2DP);

    }

    private void connectBle(final String mac) {
        if (!BleManager.getInstance().isBlueEnable()) {
            return;
        }
        List<BleDevice> allConnectedDevice = BleManager.getInstance().getAllConnectedDevice();
        if (allConnectedDevice.size() > 0) {
            return;
        }


        BleManager.getInstance().connect(mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {

                connectState = BluetoothProfile.STATE_CONNECTING;
                notifyConnectStateChange(bleDevice.getDevice(), BluetoothProfile.STATE_CONNECTED, BluetoothProfile.STATE_CONNECTING);
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                userBindDevice = false;
                connectState = BluetoothProfile.STATE_DISCONNECTED;
                notifyDeviceConnectTimeout(bleDevice.getMac());
            }

            @Override
            public void onConnectSuccess(BleDevice device, BluetoothGatt gatt, int status) {
                bleDevice = device;
                SPUtils.setBLEMac(device.getDevice().getAddress());

                userBindDevice = false;
                connectState = BluetoothProfile.STATE_CONNECTED;
                DeviceNotify(bleDevice);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {

                if (status == 99 && userBindDevice == false) {
                    connectBle(bleDevice.getMac());
                } else {
                    connectState = BluetoothProfile.STATE_DISCONNECTED;
                    notifyConnectStateChange(bleDevice.getDevice(), BluetoothProfile.STATE_CONNECTED, BluetoothProfile.STATE_DISCONNECTED);

                    if (device.getMac().equals(SPUtils.getBLEMac()) && userBindDevice == false) {
                        BleManager.getInstance().setReConnectCount(3000, 8000);
                        connectBle(bleDevice.getMac());
                    }
                }
            }
        });
    }


    private void DeviceNotify(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                BleConstantConfig.SERVICE_UUID,
                BleConstantConfig.NOTIFY_CHARACTERISTIC_UUID,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        isReady = true;
                        // 打开通知操作成功


                        BleManager.getInstance().setMtu(bleDevice, 256, new BleMtuChangedCallback() {
                            @Override
                            public void onSetMTUFailure(BleException exception) {
                                // 设置MTU失败
                            }

                            @Override
                            public void onMtuChanged(int mtu) {
                                // 设置MTU成功，并获得当前设备传输支持的MTU值

                                notifyConnectStateChange(bleDevice.getDevice(), BluetoothProfile.STATE_CONNECTING, BluetoothProfile.STATE_CONNECTED);


                                String bleMac = bleDevice.getDevice().getAddress();
                                String btMac;

                                byte uu = (byte) 0x55;
                                String substring = bleMac.substring(15, 17);
                                String bt1 = bleMac.substring(0, 15);
                                int x = Integer.parseInt(substring, 16);
                                String s = ByteUtil.bytesToHex1((byte) (x ^ uu));
                                btMac = (bt1 + s).toUpperCase(Locale.ROOT);

                                BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                                BluetoothDevice remoteDevice = defaultAdapter.getRemoteDevice(btMac);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    BtUtils.getInstance().pinTargetDevice(mA2dp, remoteDevice);
                                } else {
                                    BtUtils.getInstance().connectA2dp(mA2dp, remoteDevice);
                                }


                            }
                        });
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        notifyDeviceDataChanged(new BluetoothDeviceData(bleDevice.getMac(),
                                UUID.fromString(BleConstantConfig.NOTIFY_CHARACTERISTIC_UUID), data));

                    }
                });
    }

    private int connectState = BluetoothProfile.STATE_DISCONNECTED;


    BluetoothA2dp mA2dp;

    /**
     * blueA2dp 监听
     */
    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                mA2dp = null;
            }


        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                mA2dp = (BluetoothA2dp) proxy; //转换
            }
        }
    };

    /***
     * 连接设备
     * 注意:建议在主线程中调用，三星部分手机只能在主线程调用
     */
    @Override
    protected boolean firDeviceConnect(int type) {
        if (null == bluetoothDevice) {
            return false;
        }
        userBindDevice = true;
        FirBluetoothManager.getInstance().disconnectAllDevice();

        if (type == 1) {
            bleDevice = new BleDevice(bluetoothDevice, -90, null, System.currentTimeMillis());
            SPUtils.setBLEMac(bluetoothDevice.getAddress());
            connectBle(bluetoothDevice.getAddress());
        }
        return true;
    }


    /**
     * 销毁实例
     */
    public void destroy() {
        close();
    }

    /**
     * 写数据
     * 注意:
     * 1、BLE特征一次写入的最大字节是20个. 超过20的会丢弃
     * 2、需要在UI线程中写数据，否则接受不到回调
     * 3、为了保证数据能正常发送，延迟了100ms去发送，如果有问题，需自行修改
     *
     * @param data
     */
    @Override
    protected void sendFirDeviceData(byte[] data) {

        if (!BleManager.getInstance().isConnected(bleDevice)) {
            return;
        }
        BleManager.getInstance().write(bleDevice, BleConstantConfig.SERVICE_UUID, BleConstantConfig.WRITE_CHARACTERISTIC_UUID, data, false, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                // 写数据回调通知
                notifyDeviceDataWrite(new BluetoothDeviceData(bleDevice.getMac(),
                        UUID.fromString(BleConstantConfig.WRITE_CHARACTERISTIC_UUID), data), 0);
            }

            @Override
            public void onWriteFailure(BleException exception) {
                BleManager.getInstance().disconnect(bleDevice);

                // 写数据回调通知
                notifyDeviceDataWrite(new BluetoothDeviceData(bleDevice.getMac(),
                        UUID.fromString(BleConstantConfig.WRITE_CHARACTERISTIC_UUID), data), 1);
            }
        });
    }


    /**
     * 写数据
     * 注意:
     * 1、BLE特征一次写入的最大字节是20个. 超过20的会丢弃
     * 2、需要在UI线程中写数据，否则接受不到回调
     * 3、为了保证数据能正常发送，延迟了100ms去发送，如果有问题，需自行修改
     *
     * @param data
     */
    public void writeCharacteristic1(final byte[] data) {

        BleManager.getInstance().write(bleDevice, BleConstantConfig.SERVICE_UUID, BleConstantConfig.WRITE_CHARACTERISTIC_UUID, data, false, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                // 写数据回调通知
                notifyDeviceDataWrite(new BluetoothDeviceData(bleDevice.getMac(),
                        UUID.fromString(BleConstantConfig.WRITE_CHARACTERISTIC_UUID), data), 0);
            }

            @Override
            public void onWriteFailure(BleException exception) {
                if (bleDevice != null) {
                    // 写数据回调通知
                    notifyDeviceDataWrite(new BluetoothDeviceData(bleDevice.getMac(),
                            UUID.fromString(BleConstantConfig.WRITE_CHARACTERISTIC_UUID), data), 1);
                }
            }
        });


    }


    /**
     * 设置特性通知是否启用indication
     *
     * @param
     * @param
     */
    public void setCharacteristicIndication(boolean isEnable) {
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }


    public int getState() {
        return connectState;
    }

    public String getDeviceMac() {
        return SPUtils.getBLEMac();
    }

    public String getName() {
        if (null == bluetoothDevice) {
            return "";
        } else {
            return bluetoothDevice.getName();
        }
    }

    public boolean isReady() {
        return isReady;
    }


    /**
     * 断开连接, 如需重新连接, 可以通过bluetoothGatt.connect()重新连接
     */


    @Override
    protected void disFirDeviceConnect() {

        Log.e(TAG, "disFirDeviceConnect   deviceMode: " + deviceMode);

    }

    @Override
    protected boolean isFirDeviceConnected() {
//        Log.e(TAG, "isFirDeviceConnected   bleDevice: " + bleDevice);
        return BleManager.getInstance().isConnected(SPUtils.getBLEMac());
    }

    /**
     * 关闭蓝牙连接, 释放Gatt资源, 如需重新连接, 需要通过BluetoothDevice.connectGatt连接
     * 供外部调用, 手动关闭连接后, 不触发重连机制
     */
    @Override
    protected void firDeviceClose() {
        close(false);
    }

    /**
     * 关闭蓝牙连接, 释放Gatt资源, 如需重新连接, 需要通过BluetoothDevice.connectGatt连接
     */
    private void close(boolean isRetry) {
        connectState = BluetoothProfile.STATE_DISCONNECTED;
        isReady = false;
    }
}
