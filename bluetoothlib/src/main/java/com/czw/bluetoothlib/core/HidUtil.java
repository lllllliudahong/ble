package com.czw.bluetoothlib.core;

import static com.jieli.jl_rcsp.util.CrashHandler.TAG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.czw.bluetoothlib.app.BlueApplication;

import java.lang.reflect.Method;

public class HidUtil {

    static HidUtil instance;
    static Context context;
    BluetoothAdapter mBtAdapter;
    BluetoothProfile mBluetoothProfile;

    public static HidUtil getInstance(Context context) {
        if (null == instance) {
            instance = new HidUtil(context);
        }
        return instance;
    }

    private HidUtil(Context context) {
        this.context = context;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            mBtAdapter.getProfileProxy(context,    mListener, BluetoothProfile.HID_DEVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getProfileProxy() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultAdapter.getProfileProxy(BlueApplication.getApplication(), mListener, BluetoothProfile.HID_DEVICE);

    }


    private void registerBluetoothListener() {
// 初始化广播接收者

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
        context.registerReceiver(receiver, intentFilter);

    }

    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.i(TAG, "mConnectListener onServiceConnected");
            //BluetoothProfile proxy这个已经是BluetoothInputDevice类型了
            try {
                mBluetoothProfile = proxy;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.i(TAG, "mConnectListener onServiceConnected");
        }
    };


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // 通过广播接收到了BluetoothDevice
                final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null) return;
                String btname = device.getName();
                String address = device.getAddress();
                Log.i(TAG, "bluetooth name:" + btname + ",address:" + address);
            }
            if (action.equals("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "state=" + state + ",device=" + device);
                if (state == BluetoothProfile.STATE_CONNECTED) {//连接成功

                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {//连接失败

                }
            }

            if (action.equals("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "state=" + state + ",device=" + device);
                if (state == BluetoothProfile.STATE_CONNECTED) {//连接成功

                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {//连接失败

                }
            }


        }
    };


    /**
     * 配对
     *
     * @param BluetoothDevice
     */
    public void pair(BluetoothDevice device) {
        Log.i(TAG, "pair device:" + device);
        Method createBondMethod;
        try {
            createBondMethod = BluetoothDevice.class.getMethod("createBond");
            createBondMethod.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接设备
     *
     * @param bluetoothDevice
     */
    public void connect(final BluetoothDevice device) {
        Log.i(TAG, "connect device:" + device);
        try {
            //得到BluetoothInputDevice然后反射connect连接设备
            Method method = mBluetoothProfile.getClass().getMethod("connect",
                    new Class[]{BluetoothDevice.class});
            method.invoke(mBluetoothProfile, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开连接
     *
     * @param BluetoothDevice
     */
    public void disConnect(BluetoothDevice device) {
        Log.i(TAG, "disConnect device:" + device);
        try {
            if (device != null) {
                Method method = mBluetoothProfile.getClass().getMethod("disconnect",
                        new Class[]{BluetoothDevice.class});
                method.invoke(mBluetoothProfile, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
