package com.czw.bluetoothlib.core.jl;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.czw.bluetoothlib.app.BlueApplication;
import com.czw.bluetoothlib.bean.BleConstantConfig;
import com.czw.bluetoothlib.core.connect.BluetoothDiscovery;
import com.czw.bluetoothlib.util.ByteUtil;
import com.jieli.bluetooth_connect.bean.BluetoothOption;
import com.jieli.bluetooth_connect.bean.ErrorInfo;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.constant.JL_DeviceType;
import com.jieli.bluetooth_connect.data.HistoryRecordDbHelper;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.bluetooth_connect.interfaces.listener.OnBtDiscoveryListener;
import com.jieli.bluetooth_connect.tool.BluetoothEventCbManager;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.CHexConverter;
import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.RcspAuth;
import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.NotifyCommunicationWayCmd;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.util.CommandBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 蓝牙辅助类
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
@SuppressLint("MissingPermission")
public class BluetoothHelper extends BluetoothEventListener{
    private final static String TAG = BluetoothHelper.class.getSimpleName();
    private volatile static BluetoothHelper instance;
    private final BluetoothManager mBluetoothOp;
    private BluetoothOption bluetoothOption;
    private final RcspAuth mRcspAuth;
    private final BtEventCbManager mBtEventCbManager;
    private BluetoothDiscovery mBluetoothDiscovery;
    private BluetoothEventCbManager mEventCbManager;

    private final Map<String, Boolean> mAuthDeviceMap = new HashMap<>();

    private ChangeBleMtuTimeoutTask mChangeBleMtuTimeoutTask;
    private ConnectSppParam connectSppParam;    //连接SPP参数
    private final List<String> bleToSppList = new ArrayList<>();

    private static final long DELAY_WAITING_TIME = 5000L;
    private final static int CHECK_DELAY = 3000;

    private final static int MSG_CHECK_BLE_DISCONNECT = 0x0111;
    private final static int MSG_CHECK_AUTH = 0x112;
    private final Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (MSG_CHECK_BLE_DISCONNECT == msg.what) {
                if (connectSppParam != null) {
                    BluetoothDevice bleDev = BluetoothUtil.getRemoteDevice(connectSppParam.getBleAddress());
                    if (isConnectedBtDevice(bleDev)) {
                        JL_Log.i(TAG, "Ble is connected. so disconnect ble...");
                        disconnectDevice(bleDev);
                    }
                }
            } else if (MSG_CHECK_AUTH == msg.what) {
                handleDeviceConnectedEvent(mBluetoothOp.getConnectedDevice());
            }
            return true;
        }
    });

    private BluetoothHelper() {
        bluetoothOption = BluetoothOption.createDefaultOption();
        if (HealthConstant.DEFAULT_CONNECT_WAY == BluetoothConstant.PROTOCOL_TYPE_SPP) {
            bluetoothOption.setPriority(BluetoothConstant.PROTOCOL_TYPE_SPP);
            bluetoothOption.setScanFilterData("");
        } else {
            bluetoothOption.setPriority(BluetoothConstant.PROTOCOL_TYPE_BLE);
            bluetoothOption.setNeedChangeBleMtu(true)
                    .setMtu(BluetoothConstant.BLE_MTU_MAX);
            bluetoothOption.setBleScanStrategy(BluetoothConstant.HASH_FILTER);
        }
        bluetoothOption.setUseMultiDevice(false);

        // bluetoothOption.setBleUUID()
        this.mBluetoothDiscovery = new BluetoothDiscovery(BlueApplication.getApplication(), bluetoothOption, this.mOnBtDiscoveryListener);

        mBluetoothOp = new BluetoothManager(BlueApplication.getApplication(), bluetoothOption);

        mBluetoothOp.unregisterBluetoothCallback(mBtEventCallback);
        mBluetoothOp.registerBluetoothCallback(mBtEventCallback);
        mEventCbManager = new BluetoothEventCbManager();
        mRcspAuth = new RcspAuth(this::sendDataToDevice, mRcspAuthListener);
        mBtEventCbManager = new BtEventCbManager();
        mBluetoothOp.fastConnect();


    }

    public static BluetoothHelper getInstance() {
        if (null == instance) {
            synchronized (BluetoothHelper.class) {
                if (null == instance) {
                    instance = new BluetoothHelper();
                }
            }
        }
        return instance;
    }


    public BluetoothOption getBluetoothOption() {
        return bluetoothOption;
    }

    public BluetoothManager getmBluetoothOp() {
        return mBluetoothOp;
    }


    public void disconnectDevice2( BluetoothDevice device) {
        if (null == device) return;
        //1. 初始化蓝牙管理器，建议单例模式使用

        //2. 判断设备是否已连接
        if (mBluetoothOp.isConnectedDevice(device)) { //设备已连接
            //2.1 判断设备是否BLE连接
            if (mBluetoothOp.isConnectedBLEDevice(device)) {
                //断开BLE 通道
                mBluetoothOp.disconnectBLEDevice(device);
            } else {
                //断开SPP 通道
//                mBluetoothOp.disconnectSPPDevice(device);
            }
        }
    }

    public void addBluetoothEventListener(BluetoothEventListener listener) {
        mBtEventCbManager.addBluetoothEventListener(listener);
    }

    public void removeBluetoothEventListener(BluetoothEventListener listener) {
        mBtEventCbManager.removeBluetoothEventListener(listener);
    }

    public void destroy() {
        mBluetoothOp.unregisterBluetoothCallback(mBtEventCallback);
        mBluetoothOp.destroy();
        this.mBluetoothDiscovery.removeListener(this.mOnBtDiscoveryListener);
        this.mBluetoothDiscovery.destroy();
//        mEventCbManager.unregisterBluetoothCallback(mBtEventCallback);
        mRcspAuth.removeListener(mRcspAuthListener);
        mRcspAuth.destroy();
        mAuthDeviceMap.clear();
        needUpdateDeviceMap.clear();
        mHandler.removeCallbacksAndMessages(null);
        mBtEventCbManager.destroy();
        bleToSppList.clear();
        instance = null;
    }

    public BluetoothManager getBluetoothOp() {
        return mBluetoothOp;
    }

    public void saveHistoryRecordDbHelper(BluetoothDevice device) {
        HistoryRecordDbHelper mHistoryRecordDbHelper = getBluetoothOp().getHistoryRecordHelper();
        if (null != mHistoryRecordDbHelper) {
            HistoryRecord record = mHistoryRecordDbHelper.getHistoryRecordByMac(device.getAddress());
            if (record != null) {
                com.jieli.jl_rcsp.util.JL_Log.e(TAG, "updateHistoryRecordMsg : change device type: 5");
                record.setDevType(JL_DeviceType.JL_DEVICE_TYPE_WATCH);
                mHistoryRecordDbHelper.updateHistoryRecord(record);
            }
        }
    }

    public void updateDeviceInfo(BluetoothDevice device, int sdkFlag, String mappedAddress) {
        HistoryRecordDbHelper mHistoryRecordDbHelper = getBluetoothOp().getHistoryRecordHelper();
        if (null != mHistoryRecordDbHelper) {
            mHistoryRecordDbHelper.updateDeviceInfo(device, sdkFlag, mappedAddress);
        }
    }

    public void updateDeviceIDs(BluetoothDevice device, int vid, int uid, int pid) {
        HistoryRecordDbHelper mHistoryRecordDbHelper = getBluetoothOp().getHistoryRecordHelper();
        if (null != mHistoryRecordDbHelper) {
            mHistoryRecordDbHelper.updateDeviceIDs(device, vid, uid, pid);
        }
    }

    public int getPriority() {
        return mBluetoothOp.getBluetoothOption().getPriority();
    }

    public boolean isConnectedBtDevice(BluetoothDevice device) {
        return mBluetoothOp.isConnectedDevice(device);
    }

    public boolean isUsedBtDevice(BluetoothDevice device) {
        return mBluetoothOp.isConnectedDevice(device) &&
                BluetoothUtil.deviceEquals(mBluetoothOp.getConnectedDevice(), device);
    }

    public boolean isHistoryRecord(String devAddress) {
        return mBluetoothOp.getHistoryRecord(devAddress) != null;
    }

    public boolean isConnectedDevice() {
        return getConnectedBtDevice() != null && isDevAuth(getConnectedBtDevice().getAddress());
    }

    public BluetoothDevice getConnectedBtDevice() {
        return mBluetoothOp.getConnectedDevice();
    }

    /**
     * 获取已连接的BLE的GATT控制对象
     *
     * @return 已连接的BLE的GATT控制对象
     */
    public BluetoothGatt getConnectedBluetoothGatt(BluetoothDevice device) {
        return mBluetoothOp.getDeviceGatt(device);
    }

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothOp.getConnectedBluetoothGatt();
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (null == device) return BluetoothConstant.CONNECT_STATE_DISCONNECT;
        int status = BluetoothConstant.CONNECT_STATE_DISCONNECT;
        if (isConnectedBtDevice(device)) {
            status = BluetoothConstant.CONNECT_STATE_CONNECTED;
        } else if (BluetoothUtil.deviceEquals(device, getBluetoothOp().getConnectingDevice())) {
            status = BluetoothConstant.CONNECT_STATE_CONNECTING;
        }
        return status;
    }


    public boolean connectDeviceWithoutRecord(BluetoothDevice device) {
        if (null == device) return false;
        return mBluetoothOp.connectBtDeviceWithoutRecord(device, BluetoothConstant.PROTOCOL_TYPE_BLE);
    }

    public boolean connectDevice(BluetoothDevice device) {
        if (null == device) return false;
        int devType = device.getType();
        int connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
        if (!HealthConstant.ONLY_CONNECT_BLE) {
            if (devType == BluetoothDevice.DEVICE_TYPE_UNKNOWN || devType == BluetoothDevice.DEVICE_TYPE_DUAL) {
                connectWay = getCacheConnectWay(device);
                if (connectWay == BluetoothConstant.PROTOCOL_TYPE_SPP) {
                    String mappedAddress = mBluetoothOp.getMappedDeviceAddress(device.getAddress());
                    if (BluetoothAdapter.checkBluetoothAddress(mappedAddress)) {
                        BluetoothDevice mappedDev = BluetoothUtil.getRemoteDevice(mappedAddress);
                        if (mappedDev != null && mappedDev.getType() != BluetoothDevice.DEVICE_TYPE_LE && mappedDev.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
                            device = mappedDev;
                        }
                    }
                }
            }
        }
        return mBluetoothOp.connectBtDevice(device, connectWay);
    }

    public boolean connectDevice(BluetoothDevice device, BleScanMessage scanMessage) {
        if (null == device) return false;
        if (null != scanMessage) {
            int connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
            if (!HealthConstant.ONLY_CONNECT_BLE) {
                connectWay = scanMessage.getConnectWay();
                if (connectWay == BluetoothConstant.PROTOCOL_TYPE_SPP) {
                    if (scanMessage.getDeviceType() == com.jieli.jl_rcsp.constant.JL_DeviceType.JL_DEVICE_TYPE_WATCH && scanMessage.getVersion() == 1) { //特殊连接方式
                        connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
                    } else {
                        BluetoothDevice edrDev = BluetoothUtil.getRemoteDevice(scanMessage.getEdrAddr());
                        if (edrDev != null) device = edrDev;
                    }
                } else if (scanMessage.isOTA()) {//升级设备
                    HistoryRecord historyRecord = mBluetoothOp.getHistoryRecord(scanMessage.getOtaBleAddress());
                    if (historyRecord != null) {//存在历史记录设备
                        historyRecord.setUpdateAddress(device.getAddress());
                        mBluetoothOp.getHistoryRecordHelper().updateHistoryRecord(historyRecord);
                    } else {//新连接一个强升设备（新回连设备）
                        needUpdateDeviceMap.put(device.getAddress(), new NeedUpdateDevice(device.getAddress(), scanMessage.getOtaBleAddress(),
                                scanMessage.getDeviceType(), scanMessage.getUid(), scanMessage.getPid()));
                    }
                }
            }
            return mBluetoothOp.connectBtDevice(device, connectWay);
        } else {
            return connectDevice(device);
        }
    }

    public boolean connectBtDevice(BluetoothDevice device) {
        return mBluetoothOp.connectBtDevice(device, BluetoothConstant.PROTOCOL_TYPE_SPP);
    }

    public void connectHistoryRecord(HistoryRecord record, OnHistoryRecordCallback callback) {
        if (null == record) return;
        mBluetoothOp.connectHistoryRecord(record, callback);
    }

    public void removeHistoryRecord(String address, OnHistoryRecordCallback callback) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) return;

        Log.e(TAG, "removeHistoryRecord--- :");

        mBluetoothOp.removeHistoryRecord(address, callback);
        mBluetoothOp.clearHistoryRecords();
    }

    public int getCacheConnectWay(BluetoothDevice device) {
        int connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
        if (mBluetoothOp.isConnectedDevice(device)) {
            if (mBluetoothOp.isConnectedSppDevice(device))
                connectWay = BluetoothConstant.PROTOCOL_TYPE_SPP;
        } else {
            HistoryRecord historyRecord = mBluetoothOp.getHistoryRecord(device.getAddress());
            if (historyRecord != null) {
                connectWay = historyRecord.getConnectType();
            }
        }
        return connectWay;
    }

    public boolean isConnectedSppDevice(BluetoothDevice device) {
        return mBluetoothOp.isConnectedSppDevice(device);
    }

    public boolean isConnectedBLEDevice(BluetoothDevice device) {
        return mBluetoothOp.isConnectedBLEDevice(device);
    }

    public int getBleMtu(BluetoothDevice device) {
        return mBluetoothOp.getBleMtu(device);
    }

    public BluetoothGatt getDeviceGatt(BluetoothDevice device) {
        return mBluetoothOp.getDeviceGatt(device);
    }

    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
//        Log.e(TAG, "sendDataToDevice:" + CHexConverter.byte2HexStr(data));
        if (null != mBluetoothOp) {
            return mBluetoothOp.sendDataToDevice(device, data);
        }
        return false;

    }

    public boolean sendBleDataToDevice(BluetoothDevice device, byte[] data) {

        if (null != mBluetoothOp) {
            if (this.isConnectedBLEDevice(device)) {
                return mBluetoothOp.writeDataToBLEDevice(
                        device,
                        UUID.fromString(BleConstantConfig.SERVICE_UUID),
                        UUID.fromString(BleConstantConfig.WRITE_CHARACTERISTIC_UUID), data);
            }
        }

        return mBluetoothOp.sendDataToDevice(device, data);
    }

    public void setBluetoothOption(BluetoothOption option) {
        mBluetoothOp.setBluetoothOption(option);
    }


    public void disconnectDeviceAll(BluetoothDevice device) {
        if (null == device) return;

        Log.e(TAG, "disconnectDeviceAll---isConnectedBtDevice:" + isConnectedBtDevice(device) + "...isConnectedBLEDevice:" + isConnectedBLEDevice(device));
        removeHistoryRecord(device.getAddress(), new OnHistoryRecordCallback() {
            @Override
            public void onSuccess(HistoryRecord historyRecord) {
                Log.e(TAG, "disconnectDeviceAll---onSuccess :" + historyRecord.getAddress());

            }

            @Override
            public void onFailed(int i, String s) {
                Log.e(TAG, "disconnectDeviceAll---onFailed :" + s);
            }
        });
    }

    public void disconnectDevice(BluetoothDevice device) {
        if (null == device) return;
        if (isConnectedBtDevice(device)) {
            mBluetoothOp.disconnectBtDevice(device);
        } else {
            publishDeviceConnectionStatus(device, BluetoothConstant.CONNECT_STATE_DISCONNECT);
        }
    }

    public boolean isAuthDevice(BluetoothDevice device) {
        return device != null && isDevAuth(device.getAddress());
    }

    public void syncEdrConnectionStatus(final BluetoothDevice device, final DeviceInfo deviceInfo) {
        if (null == deviceInfo) return;
        String edrAddress = deviceInfo.getEdrAddr();
        BluetoothDevice mEdrDevice = BluetoothUtil.getRemoteDevice(edrAddress);
        if (deviceInfo.getEdrStatus() == RcspConstant.STATUS_CLASSIC_BLUETOOTH_CONNECTED) { //设备经典蓝牙已连接
            int phoneEdrStatus = mBluetoothOp.isConnectedByProfile(mEdrDevice);
            if (phoneEdrStatus == BluetoothProfile.STATE_CONNECTED) { //设备被手机连接上
                tryToChangeActivityDevice(device, mEdrDevice, deviceInfo);
            } else { //设备被其他手机连接上
                JL_Log.w(TAG, "设备被其他手机连接上");
                boolean ret = mBluetoothOp.startConnectByBreProfiles(mEdrDevice);
                JL_Log.w(TAG, "尝试连接，结果 = " + (ret ? "设备开始连接" : "设备连接失败"));
            }
        } else { //设备经典蓝牙未连接
            boolean ret = mBluetoothOp.startConnectByBreProfiles(mEdrDevice);
            JL_Log.w(TAG, ret ? "设备开始连接" : "设备连接失败");
        }
    }

    public boolean isBleChangeSpp(BluetoothDevice device) {
        return device != null && connectSppParam != null && device.getAddress().equals(connectSppParam.getBleAddress());
    }

    public boolean isBleToSpp(BluetoothDevice device) {
        return device != null && bleToSppList.contains(device.getAddress());
    }

    public void bleChangeSpp(@NonNull RcspOpImpl rcspOp, @NonNull BluetoothDevice device) {
        DeviceInfo deviceInfo = rcspOp.getDeviceInfo(device);
        if (deviceInfo != null) {

        }
        //设置回连信息
//        boolean isNeedWaitBlePair =deviceInfo==null?true: deviceInfo.getSdkType() == JLChipFlag.JL_CHIP_FLAG_701X_WATCH
//                && deviceInfo==null?true: device.getBondState() != BluetoothDevice.BOND_BONDED;

        connectSppParam = new ConnectSppParam(device.getAddress(), deviceInfo.getEdrAddr());
        /*if (isNeedWaitBlePair) {
            JL_Log.i(TAG, "device is connection at first. We will waiting for ble pair.");
            final String bleAddr = connectSppParam.getBleAddress();
            final BluetoothDevice sppDevice = BluetoothUtil.getRemoteDevice(connectSppParam.getSppAddress());
            mBluetoothOp.registerBluetoothCallback(new BluetoothEventCallback() {
                @Override
                public void onBondStatus(BluetoothDevice device, int status) {
                    JL_Log.w(TAG, String.format(Locale.getDefault(), "onBondStatus >> device : %s, status : %d.",
                            BluetoothUtil.printBtDeviceInfo(device), status));
                    if (device.getAddress().equals(bleAddr)) {
                        if (status != BluetoothDevice.BOND_BONDING) {
                            mBluetoothOp.unregisterBluetoothCallback(this);
                            if (status == BluetoothDevice.BOND_BONDED) {
                                JL_Log.i(TAG, "ble is bond. now, connect spp. " + BluetoothUtil.printBtDeviceInfo(sppDevice));
                                bleChangeSpp(rcspOp, device);
                            } else {
                                JL_Log.w(TAG, "ble pair failed.now, connect spp. " + BluetoothUtil.printBtDeviceInfo(sppDevice));
                                mBluetoothOp.connectBtDevice(sppDevice, BluetoothConstant.PROTOCOL_TYPE_SPP);
                            }
                        }
                    }
                }
            });
            return;
        }*/

        NotifyCommunicationWayCmd notifyCommunicationWayCmd = CommandBuilder.
                buildNotifyCommunicationWayCmd(BluetoothConstant.PROTOCOL_TYPE_SPP, 0);
        rcspOp.sendRcspCommand(device, notifyCommunicationWayCmd, new RcspCommandCallback<NotifyCommunicationWayCmd>() {
            @Override
            public void onCommandResponse(BluetoothDevice device, NotifyCommunicationWayCmd cmd) {
                if (cmd.getStatus() != StateCode.STATUS_SUCCESS) {
                    BaseError error = new BaseError(RcspErrorCode.ERR_RESPONSE_BAD_STATUS, "Device reply an bad status : " + cmd.getStatus());
                    error.setOpCode(cmd.getId());
                    onErrCode(device, error);
                    return;
                }
                //开始准备切换SPP, 启动等待超时
                JL_Log.i(TAG, "Waiting for ble disconnect...");
                mHandler.removeMessages(MSG_CHECK_BLE_DISCONNECT);
                mHandler.sendEmptyMessageDelayed(MSG_CHECK_BLE_DISCONNECT, CHECK_DELAY);
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                JL_Log.w(TAG, "bleChangeSpp >> onErrCode = " + error);
                if (mHandler.hasMessages(MSG_CHECK_BLE_DISCONNECT)) {//如果还有超时任务，立即触发
                    mHandler.removeMessages(MSG_CHECK_BLE_DISCONNECT);
                    mHandler.sendEmptyMessage(MSG_CHECK_BLE_DISCONNECT);
                }
            }
        });
    }

    private boolean startChangeMtu(BluetoothDevice device, int changeMtu) {
        if (mChangeBleMtuTimeoutTask != null) {
            JL_Log.w(TAG, "-startChangeMtu- Adjusting the MTU for BLE");
            return true;
        }
        boolean ret = mBluetoothOp.requestBleMtu(device, changeMtu);
        JL_Log.i(TAG, "-startChangeMtu- requestBleMtu = " + ret + ", change mtu = " + changeMtu);
        if (ret) {
            mChangeBleMtuTimeoutTask = new ChangeBleMtuTimeoutTask(device);
            mHandler.postDelayed(mChangeBleMtuTimeoutTask, DELAY_WAITING_TIME);
        }
        return ret;
    }

    private void stopChangeBleMtu() {
        JL_Log.i(TAG, "-stopChangeBleMtu- >>>>");
        if (mChangeBleMtuTimeoutTask != null) {
            mHandler.removeCallbacks(mChangeBleMtuTimeoutTask);
            mChangeBleMtuTimeoutTask = null;
        }
    }

    private boolean isDevAuth(String address) {
        if (!getBluetoothOp().getBluetoothOption().isUseDeviceAuth()) return true;
        Boolean b = mAuthDeviceMap.get(address);
        return b != null && b;
    }

    private void setDevAuth(BluetoothDevice device, boolean b) {
        if (null == device) return;
        mAuthDeviceMap.put(device.getAddress(), b);
    }

    private void removeDevAuth(String address) {
        mAuthDeviceMap.remove(address);
    }

    private void publishDeviceConnectionStatus(BluetoothDevice device, int status) {
        JL_Log.i(TAG, String.format(Locale.getDefault(), "-publishDeviceConnectionStatus- device : %s, status: %d",
                BluetoothUtil.printBtDeviceInfo(device), status));
        if (BluetoothConstant.CONNECT_STATE_CONNECTED == status || BluetoothConstant.CONNECT_STATE_DISCONNECT == status) {
            if (mChangeBleMtuTimeoutTask != null && BluetoothUtil.deviceEquals(device, mChangeBleMtuTimeoutTask.getDevice())) {
                stopChangeBleMtu();
            }
            if (BluetoothConstant.CONNECT_STATE_DISCONNECT == status && device != null) {
                removeDevAuth(device.getAddress());
                handleSppReconnectEvent(device, connectSppParam); //处理SPP回连事件
            }
        }
        mBtEventCbManager.onConnection(device, status);
    }

    private final Map<String, NeedUpdateDevice> needUpdateDeviceMap = new HashMap<>();  //需要更新的设备信息

    private void callbackDeviceConnected(BluetoothDevice device) {
        JL_Log.i(TAG, "-callbackDeviceConnected- device = " + device);
        NeedUpdateDevice needUpdateDevice = needUpdateDeviceMap.get(device.getAddress());
        if (needUpdateDevice != null) {//没有历史记录的新回连设备
            HistoryRecord historyRecord = getBluetoothOp().getHistoryRecord(device.getAddress());
            JL_Log.i(TAG, "-callbackDeviceConnected-  obtain historyRecord,  " + historyRecord);
            if (historyRecord != null) {
                historyRecord.setAddress(needUpdateDevice.getOriginalBleAddress());
                historyRecord.setUpdateAddress(device.getAddress());
                historyRecord.setDevType(needUpdateDevice.getDeviceType());
                historyRecord.setUid(needUpdateDevice.getUid());
                historyRecord.setPid(needUpdateDevice.getPid());
                mBluetoothOp.getHistoryRecordHelper().updateHistoryRecord(historyRecord);
                JL_Log.i(TAG, "-callbackDeviceConnected-  change historyRecord before, " + historyRecord);
            }
            needUpdateDeviceMap.remove(device.getAddress());
        }

        publishDeviceConnectionStatus(device, BluetoothConstant.CONNECT_STATE_CONNECTED);
    }

    private void handleDeviceConnectedEvent(BluetoothDevice device) {
        int connectWay = mBluetoothOp.isConnectedSppDevice(device) ? BluetoothConstant.PROTOCOL_TYPE_SPP : BluetoothConstant.PROTOCOL_TYPE_BLE;
        JL_Log.d(TAG, "-handleDeviceConnectedEvent- device = " + BluetoothUtil.printBtDeviceInfo(device) + ", connectWay = " + connectWay);
        //Step0.检测设备是否通过设备认证
        if (!isAuthDevice(device)) { //设备未认证
            mRcspAuth.stopAuth(device);
            boolean ret = mRcspAuth.startAuth(device);
            JL_Log.i(TAG, "-handleDeviceConnectedEvent- startAuth = " + ret);
            if (!ret) {
                disconnectDevice(device);
            }
            return;
        }
        //Step1.分类型处理不同的连接情况
        switch (connectWay) {
            case BluetoothConstant.PROTOCOL_TYPE_BLE:
                int mtu = mBluetoothOp.getBleMtu(device);
                int changeMtu = mBluetoothOp.getBluetoothOption().getMtu();
                if (mtu != changeMtu && mBluetoothOp.getBluetoothOption().isNeedChangeBleMtu()) {
                    boolean ret = startChangeMtu(device, changeMtu);
                    JL_Log.i(TAG, "-handleDeviceConnectedEvent- startChangeMtu = " + ret + ", mtu = " + mtu + ", change mtu = " + changeMtu);
                    if (ret) {
                        return;
                    }
                }
                break;
            case BluetoothConstant.PROTOCOL_TYPE_SPP:
                break;
        }
        //Step2.回调连接成功
        callbackDeviceConnected(device);
    }

    private void handleReceiveRawData(BluetoothDevice device, byte[] rawData) {
        boolean isAuthDevice = isAuthDevice(device);
        JL_Log.d(TAG, "-handleReceiveRawData- device = " + device + ", isAuthDevice : " + isAuthDevice
                + ", rawData = " + CHexConverter.byte2HexStr(rawData));
        if (!isAuthDevice) {
            mRcspAuth.handleAuthData(device, rawData);
        } else {
            mBtEventCbManager.onReceiveData(device, rawData);
        }
    }

    private BluetoothDevice getCacheEdrDevice(BluetoothDevice device, DeviceInfo deviceInfo) {
        if (device == null) return null;
        BluetoothDevice mTargetDevice = null;
        if (deviceInfo != null) {
            BluetoothDevice edrDev = BluetoothUtil.getRemoteDevice(deviceInfo.getEdrAddr());
            if (edrDev != null) {
                mTargetDevice = edrDev;
            }
        }
        if (mTargetDevice == null) {
            HistoryRecord historyRecord = mBluetoothOp.getHistoryRecord(device.getAddress());
            if (null != historyRecord) {
                String edrAddr;
                if (historyRecord.getConnectType() == BluetoothConstant.PROTOCOL_TYPE_BLE) {
                    edrAddr = historyRecord.getMappedAddress();
                } else {
                    edrAddr = historyRecord.getAddress();
                }
                JL_Log.d(TAG, "-getCacheEdrDevice- edrAddr :" + edrAddr);
                BluetoothDevice temp = BluetoothUtil.getRemoteDevice(edrAddr);
                if (temp != null && (temp.getType() != BluetoothDevice.DEVICE_TYPE_LE &&
                        temp.getType() != BluetoothDevice.DEVICE_TYPE_DUAL)) {
                    mTargetDevice = temp;
                }
            }
        }
        if (mTargetDevice == null) {
            mTargetDevice = device;
        }
        return mTargetDevice;
    }

    private void tryToChangeActivityDevice(BluetoothDevice connectedDevice, BluetoothDevice mEdrDevice, DeviceInfo deviceInfo) {
        if (!BluetoothConstant.IS_CHANGE_ACTIVITY_DEVICE || connectedDevice == null || mEdrDevice == null)
            return;
        BluetoothDevice currentDev = mBluetoothOp.getActivityBluetoothDevice();
        BluetoothDevice useDevice = mBluetoothOp.getConnectedDevice();
        if (BluetoothUtil.deviceEquals(connectedDevice, useDevice)) {
            if (!BluetoothUtil.deviceEquals(currentDev, mEdrDevice)) {
                boolean setRet = mBluetoothOp.setActivityBluetoothDevice(mEdrDevice);
                JL_Log.i(TAG, "-tryToChangeActivityDevice- setActivityBluetoothDevice >> " + setRet +
                        ", mEdrDevice : " + BluetoothUtil.printBtDeviceInfo(mEdrDevice));
            }
        } else {
            BluetoothDevice mTargetEdrDev = getCacheEdrDevice(useDevice, deviceInfo);
            if (!BluetoothUtil.deviceEquals(currentDev, mTargetEdrDev)) {
                boolean setRet = mBluetoothOp.setActivityBluetoothDevice(mTargetEdrDev);
                JL_Log.i(TAG, "-tryToChangeActivityDevice- setActivityBluetoothDevice >> " + setRet +
                        ", mTargetEdrDev : " + BluetoothUtil.printBtDeviceInfo(mTargetEdrDev));
            }
        }
    }

    private void resetConnectSppParam() {
        connectSppParam = null;
    }

    private void handleSppReconnectEvent(BluetoothDevice device, ConnectSppParam connectSppParam) {
        JL_Log.i(TAG, "handleSppReconnectEvent. " + device + ", " + connectSppParam);
        if (connectSppParam != null && device.getAddress().equals(connectSppParam.bleAddress)) {
            mHandler.removeMessages(MSG_CHECK_BLE_DISCONNECT); //移除超时任务
            final String address = connectSppParam.getSppAddress();
            //开始回连设备
            final BluetoothDevice sppDevice = BluetoothUtil.getRemoteDevice(address);
            if (null == sppDevice) {
                JL_Log.w(TAG, "not found device. " + address);
                resetConnectSppParam();
                return;
            }
            JL_Log.w(TAG, String.format(Locale.getDefault(), "ble[%s] is disconnected. ready to connect spp[%s].", device.getAddress(), address));
            //等待500ms, 目的是等待EDR开启，加快SPP连接
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetConnectSppParam();
                    bleToSppList.add(sppDevice.getAddress());
                    JL_Log.i(TAG, "start connect spp. " + BluetoothUtil.printBtDeviceInfo(sppDevice));
                    mBluetoothOp.connectSPPDevice(sppDevice);
                }
            }, 1500);
        }
    }

    private final BluetoothEventCallback mBtEventCallback = new BluetoothEventCallback() {

        @Override
        public void onAdapterStatus(boolean bEnabled, boolean bHasBle) {
            mBtEventCbManager.onAdapterStatus(bEnabled);
        }

        @Override
        public void onDiscoveryStatus(boolean bBle, boolean bStart) {
            mBtEventCbManager.onBtDiscoveryStatus(bBle, bStart);
        }

        @Override
        public void onDiscovery(BluetoothDevice device, BleScanMessage bleScanMessage) {
            mBtEventCbManager.onBtDiscovery(device, bleScanMessage);
        }

        @Override
        public void onShowDialog(BluetoothDevice device, BleScanMessage bleScanMessage) {
            mBtEventCbManager.onShowDialog(device, bleScanMessage);
        }

        @Override
        public void onConnection(BluetoothDevice device, int status) {
            JL_Log.w(TAG, "onConnection >>> status = " + status);
            if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) {
                JLDeviceNotification.getInstance().enableBLEDeviceNotification(mBluetoothOp.getDeviceGatt(device), UUID.fromString(BleConstantConfig.SERVICE_UUID),
                        UUID.fromString(BleConstantConfig.NOTIFY_CHARACTERISTIC_UUID));
                mHandler.sendEmptyMessageDelayed(MSG_CHECK_AUTH, 1500);
            } else {
                publishDeviceConnectionStatus(device, status);
            }
        }

        @Override
        public void onSwitchConnectedDevice(BluetoothDevice device) {
            mBtEventCbManager.onSwitchConnectedDevice(device);
        }

        @Override
        public void onHistoryRecordChange(int op, HistoryRecord record) {
            mBtEventCbManager.onHistoryRecord(op, record);
        }

        @Override
        public void onBleDataBlockChanged(BluetoothDevice device, int block, int status) {
            mBtEventCbManager.onBleMtuChange(getConnectedBluetoothGatt(device), block, status);
            if (status == BluetoothGatt.GATT_SUCCESS && mChangeBleMtuTimeoutTask != null && BluetoothUtil.deviceEquals(device, mChangeBleMtuTimeoutTask.getDevice())) {
                stopChangeBleMtu();
                callbackDeviceConnected(device);
            }
        }

        @Override
        public void onBleDataNotification(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {
            JL_Log.w(TAG, "onBleDataNotification >>> serviceUuid = " + serviceUuid + "---: " + ByteUtil.bytesToHexSegment(data));
            if (serviceUuid.equals(mBluetoothOp.getBluetoothOption().getBleServiceUUID())
                    && characteristicsUuid.equals(mBluetoothOp.getBluetoothOption().getBleNotificationUUID())) {
                handleReceiveRawData(device, data);
            } else if (serviceUuid.equals(UUID.fromString(BleConstantConfig.SERVICE_UUID)) && characteristicsUuid
                    .equals(UUID.fromString(BleConstantConfig.NOTIFY_CHARACTERISTIC_UUID))) {
                //旧版协议返回
                mBtEventCbManager.onReceiveOldData(device, data);
            }

        }

        @Override
        public void onSppDataNotification(BluetoothDevice device, UUID sppUUID, byte[] data) {
            if (sppUUID.equals(mBluetoothOp.getBluetoothOption().getSppUUID())) {
                handleReceiveRawData(device, data);
            }
        }

        @Override
        public void onError(ErrorInfo error) {
            mBtEventCbManager.onError(error);
        }
    };

    private final RcspAuth.OnRcspAuthListener mRcspAuthListener = new RcspAuth.OnRcspAuthListener() {
        @Override
        public void onInitResult(boolean b) {

        }

        @Override
        public void onAuthSuccess(BluetoothDevice bluetoothDevice) {
            JL_Log.w(TAG, "-onAuthSuccess- device : " + BluetoothUtil.printBtDeviceInfo(bluetoothDevice));
            setDevAuth(bluetoothDevice, true);
            handleDeviceConnectedEvent(bluetoothDevice);
        }

        @Override
        public void onAuthFailed(BluetoothDevice bluetoothDevice, int i, String s) {
            JL_Log.e(TAG, "-onAuthFailed- device : " + BluetoothUtil.printBtDeviceInfo(bluetoothDevice) + ", code = " + i + ", message = " + s);
            setDevAuth(bluetoothDevice, false);
            disconnectDevice(bluetoothDevice);
        }
    };

    private class ChangeBleMtuTimeoutTask implements Runnable {
        private final BluetoothDevice mDevice;

        public ChangeBleMtuTimeoutTask(BluetoothDevice device) {
            mDevice = device;
        }

        public BluetoothDevice getDevice() {
            return mDevice;
        }

        @Override
        public void run() {
            if (mBluetoothOp.isConnectedDevice(mDevice)) {
                callbackDeviceConnected(mDevice);
            } else {
                publishDeviceConnectionStatus(mDevice, BluetoothConstant.CONNECT_STATE_DISCONNECT);
            }
        }
    }

    private static class ConnectSppParam {
        private final String bleAddress;
        private final String sppAddress;

        public ConnectSppParam(String bleAddress, String sppAddress) {
            this.bleAddress = bleAddress;
            this.sppAddress = sppAddress;
        }

        public String getBleAddress() {
            return bleAddress;
        }

        public String getSppAddress() {
            return sppAddress;
        }

        @Override
        public String toString() {
            return "ConnectSppParam{" +
                    "bleAddress='" + bleAddress + '\'' +
                    ", sppAddress='" + sppAddress + '\'' +
                    '}';
        }
    }

    private final OnBtDiscoveryListener mOnBtDiscoveryListener = new OnBtDiscoveryListener() {
        public void onDiscoveryStatusChange(boolean isBle, boolean bStart) {
            BluetoothHelper.this.mEventCbManager.onDiscoveryStatus(isBle, bStart);
            if (bStart) {
                BluetoothHelper.this.syncSystemBtDeviceList(isBle);
            }

        }

        public void onDiscoveryDevice(BluetoothDevice device, BleScanMessage bleScanMessage) {
            BluetoothHelper.this.mEventCbManager.onDiscovery(device, bleScanMessage);
        }

        public void onShowProductDialog(BluetoothDevice device, BleScanMessage bleScanMessage) {
            BluetoothHelper.this.mEventCbManager.onShowDialog(device, bleScanMessage);
        }

        public void onDiscoveryError(ErrorInfo error) {
            JL_Log.e(TAG, "-onDiscoveryError- " + error);
            BluetoothHelper.this.mEventCbManager.onDiscoveryStatus(true, false);
        }
    };

    private void syncSystemBtDeviceList(boolean isBleWay) {
        List<BluetoothDevice> mConnectedList = isBleWay ? BluetoothUtil.getConnectedBleDeviceList(
                BlueApplication.getApplication()) : BluetoothUtil.getSystemConnectedBtDeviceList(BlueApplication.getApplication());
        if (null != mConnectedList) {
            String filterData = this.bluetoothOption.getScanFilterData();
            Iterator var4 = mConnectedList.iterator();

            while (true) {
                BluetoothDevice sysConnectDev;
                do {
                    do {
                        do {
                            if (!var4.hasNext()) {
                                return;
                            }

                            sysConnectDev = (BluetoothDevice) var4.next();
                        } while (this.isConnectedBtDevice(sysConnectDev));
                    } while (this.getDiscoveredBluetoothDevices().contains(sysConnectDev));
                } while ((!isBleWay || sysConnectDev.getType() != 2 && sysConnectDev.getType() != 3) && (sysConnectDev.getName() == null || filterData == null || !sysConnectDev.getName().startsWith(filterData)));

                BleScanMessage scanMessage = new BleScanMessage();
                scanMessage.setEnableConnect(true);
                this.mEventCbManager.onDiscovery(sysConnectDev, scanMessage);
            }
        }
    }

    public ArrayList<BluetoothDevice> getDiscoveredBluetoothDevices() {
        return this.mBluetoothDiscovery.getDiscoveredBluetoothDevices();
    }

    public boolean isScanning() {
        return this.mBluetoothDiscovery.isScanning();
    }

    public int getScanType() {
        return this.mBluetoothDiscovery.getScanType();
    }

    public boolean startDeviceScan(long timeout) {
        return this.mBluetoothDiscovery.startDeviceScan(timeout);
    }

    public boolean startDeviceScan(int type, long timeout) {
        return this.mBluetoothDiscovery.startDeviceScan(type, timeout);
    }

    public boolean stopDeviceScan() {
        return this.mBluetoothDiscovery.stopDeviceScan();
    }

    public boolean startBLEScan(long timeout) {
        return this.mBluetoothDiscovery.startBLEScan(timeout);
    }

    public boolean stopBLEScan() {
        return this.mBluetoothDiscovery.stopBLEScan();
    }

}
