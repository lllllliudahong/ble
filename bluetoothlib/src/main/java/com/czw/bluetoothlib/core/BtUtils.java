package com.czw.bluetoothlib.core;


import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.czw.bluetoothlib.app.BlueApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BtUtils {


    private static final String TAG = "BtUtils";
    static BtUtils utils;

    public static BtUtils getInstance() {
        if (utils == null) {
            synchronized (BtUtils.class) {
                if (utils == null) {
                    utils = new BtUtils();
                }
            }
        }
        return utils;
    }


    public void getProfileProxy() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultAdapter.getProfileProxy(BlueApplication.getApplication(), mListener, BluetoothProfile.A2DP);

    }

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


            Log.i(TAG, "onServiceDisconnected profile=" + profile);

        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                mA2dp = (BluetoothA2dp) proxy; //转换
            }

            Log.i(TAG, "onServiceConnected profile=" + profile);
        }
    };


    /**
     * 配对蓝牙设备
     * <p>
     * https://blog.csdn.net/u010356768/article/details/91493622
     * https://blog.csdn.net/weixin_41101173/article/details/116308853
     */
    public void pinTargetDevice(BluetoothA2dp mA2dp,BluetoothDevice device) {
        if (device == null) {
            return;
        }
        Log.d(TAG, "attemp to pinTargetDevice  :" + device.getAddress());

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {//没配对才配对

            if (device.getBondState() != BluetoothDevice.BOND_BONDING) {
                try {
                    Log.d(TAG, "开始配对...");
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    Boolean returnValue = (Boolean) createBondMethod.invoke(device);

                    if (returnValue) {
                        Log.d(TAG, "配对成功...");
                        connectA2dp(mA2dp, device);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }

        } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {

            connectA2dp(mA2dp, device);
        }

    }

    /**
     * 取消配对（取消配对成功与失败通过广播返回 也就是配对失败）
     *
     * @param device
     */
    public void cancelPinBule(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        Log.d(TAG, "attemp to cancelPinBule  :" + device.getAddress());
        try {
            Method removeBondMethod = device.getClass().getMethod("removeBond");
            Boolean returnValue = (Boolean) removeBondMethod.invoke(device);
            returnValue.booleanValue();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "attemp to cancel bond fail!");
        }
    }


    public void autoConnectA2dp(BluetoothDevice device) {

        Log.d(TAG, "attemp to connectA2dp  :" + device.getAddress());
        setPriority(mA2dp, device, 100); //设置priority
        try {
            //通过反射获取BluetoothA2dp中connect方法（hide的），进行连接。
            Method connectMethod = BluetoothA2dp.class.getMethod("connect",
                    BluetoothDevice.class);
            connectMethod.invoke(mA2dp, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void connectA2dp(BluetoothA2dp mA2dp, BluetoothDevice device) {
        Log.d(TAG, "attemp to connectA2dp  :" + device.getAddress());
        setPriority(mA2dp, device, 100); //设置priority
        try {
            //通过反射获取BluetoothA2dp中connect方法（hide的），进行连接。
            Method connectMethod = BluetoothA2dp.class.getMethod("connect",
                    BluetoothDevice.class);
            connectMethod.invoke(mA2dp, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disConnectA2dp(BluetoothDevice device) {
        setPriority(mA2dp, device, 0);
        try {
            //通过反射获取BluetoothA2dp中connect方法（hide的），断开连接。
            Method connectMethod = BluetoothA2dp.class.getMethod("disconnect",
                    BluetoothDevice.class);
            connectMethod.invoke(mA2dp, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPriority(BluetoothA2dp mA2dp, BluetoothDevice device, int priority) {
        if (mA2dp == null) return;
        try {//通过反射获取BluetoothA2dp中setPriority方法（hide的），设置优先级
            Method connectMethod = BluetoothA2dp.class.getMethod("setPriority",
                    BluetoothDevice.class, int.class);
            connectMethod.invoke(mA2dp, device, priority);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPriority(BluetoothA2dp mA2dp, BluetoothDevice device) {
        int priority = 0;
        if (mA2dp == null) return priority;
        try {//通过反射获取BluetoothA2dp中getPriority方法（hide的），获取优先级
            Method connectMethod = BluetoothA2dp.class.getMethod("getPriority",
                    BluetoothDevice.class);
            priority = (Integer) connectMethod.invoke(mA2dp, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return priority;
    }


    //TODO 根据mac地址判断是否已连接(这里参数可以直接用BluetoothDevice对象)
    //但这么写其实更通用。
    public boolean isConnected(String macAddress) {
        if (!BluetoothAdapter.checkBluetoothAddress(macAddress)) {
            return false;
        }
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

        Method isConnectedMethod = null;
        boolean isConnected;
        try {
            isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
            isConnectedMethod.setAccessible(true);
            isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
        } catch (NoSuchMethodException e) {
            isConnected = false;
        } catch (IllegalAccessException e) {
            isConnected = false;
        } catch (InvocationTargetException e) {
            isConnected = false;
        }
        return isConnected;
    }




}



