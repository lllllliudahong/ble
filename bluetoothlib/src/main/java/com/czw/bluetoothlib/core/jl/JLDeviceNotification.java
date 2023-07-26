package com.czw.bluetoothlib.core.jl;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.SystemClock;

import com.jieli.bluetooth_connect.util.JL_Log;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class JLDeviceNotification {

    private static final String TAG = "JLDeviceNotification";

    private volatile static JLDeviceNotification instance;

    public static JLDeviceNotification getInstance() {
        if (null == instance) {
            synchronized (JLDeviceNotification.class) {
                if (null == instance) {
                    instance = new JLDeviceNotification();
                }
            }
        }
        return instance;
    }

    public boolean enableBLEDeviceNotification(BluetoothGatt bluetoothGatt, UUID serviceUUID, UUID characteristicUUID) {
        if (null == bluetoothGatt) {
            JL_Log.w(TAG, "bluetooth gatt is null....");
            return false;
        } else {
            BluetoothGattService gattService = bluetoothGatt.getService(serviceUUID);
            if (null == gattService) {
                JL_Log.w(TAG, "bluetooth gatt service is null....");
                return false;
            } else {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(characteristicUUID);
                if (null == characteristic) {
                    JL_Log.w(TAG, "bluetooth characteristic is null....");
                    return false;
                } else {
                    boolean bRet = bluetoothGatt.setCharacteristicNotification(characteristic, true);
                    if (bRet) {
                        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                        if (descriptors == null || descriptors.size() == 0) {
                            bRet = false;
                        }

                        if (descriptors != null) {
                            JL_Log.d(TAG, "descriptors size = " + descriptors.size());
                            Iterator var9 = descriptors.iterator();

                            while (var9.hasNext()) {
                                BluetoothGattDescriptor descriptor = (BluetoothGattDescriptor) var9.next();
                                bRet = this.tryToWriteDescriptor(bluetoothGatt, descriptor, 0, false);
                                if (!bRet) {
                                    JL_Log.w(TAG, "tryToWriteDescriptor failed....");
                                }
                            }
                        }
                    } else {
                        JL_Log.w(TAG, "setCharacteristicNotification is failed....");
                    }

                    JL_Log.w(TAG, "enableBLEDeviceNotification ret : " + bRet);
                    return bRet;
                }
            }
        }
    }

    private boolean tryToWriteDescriptor(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor, int retryCount, boolean isSkipSetValue) {
        boolean ret = isSkipSetValue;
        if (!isSkipSetValue) {
            ret = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            JL_Log.i(TAG, "..descriptor : .setValue  ret : " + ret);
            if (!ret) {
                ++retryCount;
                if (retryCount >= 3) {
                    return false;
                }

                JL_Log.i(TAG, "-tryToWriteDescriptor- : retryCount : " + retryCount + ", isSkipSetValue :  false");
                SystemClock.sleep(50L);
                this.tryToWriteDescriptor(bluetoothGatt, descriptor, retryCount, false);
            } else {
                retryCount = 0;
            }
        }

        if (ret) {
            ret = bluetoothGatt.writeDescriptor(descriptor);
            JL_Log.i(TAG, "..bluetoothGatt : .writeDescriptor  ret : " + ret);
            if (!ret) {
                ++retryCount;
                if (retryCount >= 3) {
                    return false;
                }
                JL_Log.i(TAG, "-tryToWriteDescriptor- 2222 : retryCount : " + retryCount + ", isSkipSetValue :  true");
                SystemClock.sleep(50L);
                this.tryToWriteDescriptor(bluetoothGatt, descriptor, retryCount, true);
            }
        }

        return ret;
    }

}
