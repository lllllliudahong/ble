package com.czw.newfit.ui.search

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.czw.bluetoothlib.core.BluetoothScanner
import com.czw.bluetoothlib.core.FirBluetoothManager
import com.czw.bluetoothlib.core.jl.BluetoothHelper
import com.czw.bluetoothlib.listener.BluetoothDeviceStateListener
import com.czw.bluetoothlib.listener.BluetoothScanListener
import com.czw.bluetoothlib.util.BluetoothUtil
import com.czw.bluetoothlib.util.ByteUtil
import com.czw.newfit.R
import com.czw.newfit.adapter.IAppListView
import com.czw.newfit.api.ApiConstant
import com.czw.newfit.base.BoxActivity
import com.czw.newfit.bean.ScanDeviceBean
import com.czw.newfit.bean.onDeviceStateChange
import com.czw.newfit.databinding.ActivitySearchDeviceBinding
import com.czw.newfit.device.FreeFitDevice
import com.czw.newfit.utils.LogUtils
import com.czw.newfit.utils.PermissionUtil
import com.czw.newfit.widget.dialog.ChoiceDialog
import com.miekir.common.extension.lazy
import com.orhanobut.hawk.Hawk
import com.permissionx.guolindev.PermissionX
import org.greenrobot.eventbus.EventBus
import java.util.Locale

/**
 * 搜索蓝牙页面
 */
class SearchDeviceActivity : BoxActivity<ActivitySearchDeviceBinding>(), IAppListView, BluetoothScanListener,
    BluetoothDeviceStateListener {

    private val mPresenter: SearchDevicePresenter by lazy()

    override fun onBindingInflate() = ActivitySearchDeviceBinding.inflate(layoutInflater)

    private val TAG = "SearchDeviceActivity"

    private val mList = ArrayList<ScanDeviceBean>()
    private val macList: ArrayList<String> = java.util.ArrayList()
    private val mAdapter = SearchDeviceAdapter(mList)
    private var bluetoothScanner: BluetoothScanner? = null
    private var device: FreeFitDevice? = null
    private var connectedDevice: FreeFitDevice? = null

    override fun onInit() {

        binding.flTitleBar.setOnClickListener { finish() }
        binding.rvApp.run {
            layoutManager = LinearLayoutManager(this@SearchDeviceActivity)
            adapter = mAdapter
        }

        binding.tvResearch.setOnClickListener {
            checkPermission()
        }
        binding.ivScan.setOnClickListener {
            checkPermissionCamera()
        }
        binding.delete.setOnClickListener {
            if(device == null){
                showChoiceDialog(connectedDevice)
            }else{
                showChoiceDialog(device)
            }
        }

        mAdapter.setOnItemClickListener { adapter, view, position ->
            if (mList.size > position){
                val scanDeviceBean: ScanDeviceBean = mList[position]
                val bluetoothDevice: BluetoothDevice? = scanDeviceBean.device
                bindDevice(scanDeviceBean, bluetoothDevice)
            }
        }

        // 搜索
        bluetoothScanner = BluetoothScanner(this)
        bluetoothScanner?.setBluetoothScanListener(this)

        val deviceMac = Hawk.get<String>(ApiConstant.DEVICE)
        val deviceName = Hawk.get<String>(ApiConstant.DEVICE_NAME)
        val b = FirBluetoothManager.getInstance().getDevice(deviceMac)
        if(b != null){
            connectedDevice = b as FreeFitDevice
        }
        if (!TextUtils.isEmpty(deviceMac) && connectedDevice != null) {
            connectedDevice?.addDeviceStateListener(this)
        }
        if (!TextUtils.isEmpty(deviceMac) && connectedDevice != null && connectedDevice!!.isConnected) {
            binding.tvMyDeviceTitle.visibility = View.VISIBLE
            binding.tvDeleteDes.visibility = View.VISIBLE
            binding.esmMyDevice.visibility = View.VISIBLE
            binding.tvName.text = deviceName
            binding.tvDeviceMac.text = deviceMac
            binding.tvMyDeviceConnectState.visibility = View.VISIBLE
            binding.ivLoading.visibility = View.GONE
            binding.tvLoading.visibility = View.GONE
        } else {
            binding.tvMyDeviceTitle.visibility = View.GONE
            binding.tvDeleteDes.visibility = View.GONE
            binding.esmMyDevice.visibility = View.GONE
            binding.tvMyDeviceConnectState.visibility = View.GONE
            checkPermission()
        }
    }

    private fun checkPermission() {
        var required_permission_list2: Array<String> = PermissionUtil.getInstance().REQUIRED_PERMISSION_LIST2_1
        required_permission_list2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionUtil.getInstance().REQUIRED_PERMISSION_LIST2
        } else {
            PermissionUtil.getInstance().REQUIRED_PERMISSION_LIST2_1
        }
        PermissionX.init(this@SearchDeviceActivity)
            .permissions(permissions = required_permission_list2)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    processLogic()
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", this@SearchDeviceActivity.applicationContext.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
    }

    private fun checkPermissionCamera() {
        PermissionX.init(this@SearchDeviceActivity)
            .permissions(permissions = PermissionUtil.getInstance().REQUIRED_PERMISSION_CAMERA)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    FirBluetoothManager.getInstance().cancelScan()
                    startActivityForResult(Intent(this, ScanActivity::class.java), 22)
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", this@SearchDeviceActivity.applicationContext.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
    }

    private fun processLogic() {
        if (!BluetoothUtil.isSupportBLE(this)) {
            return
        }
        mList.clear()
        macList.clear()
        showLoadingAnim()
        bluetoothScanner?.startScan()
    }


    private fun showLoadingAnim() {
        binding.ivLoading.visibility = View.VISIBLE
        binding.tvLoading.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(this, R.anim.bluetooth_anim_loading)
        val linearInterpolator = LinearInterpolator()
        animation.interpolator = linearInterpolator
        binding.ivLoading.startAnimation(animation)
    }

    // 隐藏搜索设备动画
    private fun hideLoadingAnim() {
        binding.ivLoading.clearAnimation()
        binding.tvResearch.visibility = View.VISIBLE
        binding.tvSearchFinish.visibility = View.VISIBLE
        binding.ivLoading.visibility = View.GONE
        binding.tvLoading.visibility = View.GONE
    }


    /**
     * 所有APP加载完成
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun onAppList(it: List<String>) {
//        mList.clear()
//        mList.addAll(it)
//        mAdapter.notifyDataSetChanged()
    }

    override fun onStartScan() {
        binding.tvResearch.visibility = View.GONE
        binding.tvSearchFinish.visibility = View.GONE
        LogUtils.e(TAG,"onStartScan")
    }

    override fun onScanFailed(errorMessage: String?) {
        LogUtils.e(TAG,"onScanFailed")
        ToastUtils.showLong(errorMessage)
        binding.tvResearch.visibility = View.VISIBLE
        binding.tvSearchFinish.visibility = View.VISIBLE
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    override fun onDeviceFounded(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?, deviceMode: Int) {


//        Log.e(TAG, "B====== onDeviceFounded.--- " + device.getName());
        if(scanRecord == null) return

        if (device != null) {
            var signalLevel = 1
            if (rssi >= -60) {
                signalLevel = 5
            } else if (rssi >= -70) {
                signalLevel = 4
            } else if (rssi >= -80) {
                signalLevel = 3
            } else if (rssi >= -90) {
                signalLevel = 2
            } else if (rssi >= -100) {
                signalLevel = 1
            }
//            var scanRecordStr = ""
//            scanRecord.forEach {
//                var str = (it.toInt() and 0xff).toString(16)
//                if(str.length == 1){
//                    str = "0$str"
//                }
//                scanRecordStr += str
//            }
//            LogUtils.e(TAG,"B====== scanRecordStr.--- $scanRecordStr")
            if (device.name != null) {
                val scanDeviceBean = ScanDeviceBean(device = device, scanRecords = scanRecord, signalLevel, deviceMode)
                LogUtils.e(TAG,"B====== scanDeviceBean.--- ${ByteUtil.bytesToHex(scanDeviceBean.scanRecords)}")
//                macList.add(device.address)
                mList.add(scanDeviceBean)
                macList.add(device.address)
                mList.sortByDescending { it.signal }
                mAdapter.notifyDataSetChanged()
            }

            if (findMac != null) {
                if (device.address == findMac) {
                    val scanDeviceBean = ScanDeviceBean(device = device, scanRecords = scanRecord, signalLevel, deviceMode)
                    bindDevice(scanDeviceBean, device)
                    findMac = null
                }
            }
        }
    }

    override fun onStopScan() {
        hideLoadingAnim()
        LogUtils.e(TAG,"onStopScan")
    }

    /**
     * 绑定设备
     */
    private fun bindDevice(scanDeviceBean: ScanDeviceBean, bluetoothDevice: BluetoothDevice?) {
        bluetoothScanner?.stopScan()
        FirBluetoothManager.getInstance().cancelScan()
        showLoading("Binding", false)
//        BluetoothHelper.getInstance().disconnectDeviceAll(WatchManager.getInstance().getConnectedDevice())
//        WatchManager.getInstance().release()
        BluetoothHelper.getInstance().disconnectDevice2(bluetoothDevice)
        BluetoothHelper.getInstance().destroy()
        device = FreeFitDevice()
//        //        Log.e(TAG, " scanDeviceBean.getDeviceMode() = " + scanDeviceBean.getDeviceMode());
        Hawk.put(ApiConstant.DEVICE_MODE, scanDeviceBean.deviceMode) //  连接的设备类型
//        if (scanDeviceBean.getDeviceMode() === FirNewDeviceConstant.DEVICE_JL_MODE) {
//            WatchManager.getInstance()
//        }
//        deviceISOTA(scanDeviceBean.getScanRecords())
        device?.removeBluetoothEventListener()
        device?.addBluetoothEventListener()
        device?.setDeviceMode(scanDeviceBean.deviceMode)
        device?.bluetoothDevice = bluetoothDevice
        device?.addDeviceStateListener(this)
//        SPUtils.setDeviceMode(scanDeviceBean.getDeviceMode())
        FirBluetoothManager.getInstance().connect(device)
    }

    @SuppressLint("MissingPermission")
    override fun onStateChange(device: BluetoothDevice?, oldState: Int, newState: Int) {

        LogUtils.e(
            TAG,
            "Device state onStateChange --onStateChange-- newState:" + newState + " -- " + (if (BluetoothProfile.STATE_CONNECTED == newState) "连接" else "断开") + "--mac:" + if (device == null) "" else device.address
        )
        if (BluetoothProfile.STATE_CONNECTED == newState) {
            Hawk.put<String>(ApiConstant.DEVICE, device?.address)
            if (device?.name != null && device.name != "") {
                Hawk.put(ApiConstant.DEVICE_NAME, device.name)
            }
            hideLoading()

//            mList.forEachIndexed { index, scanDeviceBean ->
//                if ((scanDeviceBean.device?.address ?: "") == (device?.address ?: "")){
//                    mList.removeAt(index)
//                    return@forEachIndexed
//                }
//            }
            ToastUtils.showLong("OK")


            //连接上后 保存 用户设备信息到 sp 中.退出登录时 不删除该信息,只有该用户手动解绑设备,才清除该信息
            //退出登录时,只需要断开设备的连接,下次登录时,从sp 中取用户设备信息,如果有代表用户绑定过设备,需要手动去连接,否则显示去搜索的页面.
            Hawk.put(ApiConstant.USER_DEVICE_INFO, device?.name + "&" + device?.address)
//            mHandler.sendEmptyMessageDelayed(MSG_BLE_CONNECT_OK, 1000) //显示申请权限对话框
            if (this.device != null) {
                //  没绑定的提示绑定
                this.device!!.bindDevice()
            }
            finish()
        }

        EventBus.getDefault().post(onDeviceStateChange(newState, device))
    }

    override fun onReady(deviceMac: String?) {
    }

    override fun onDiscoverServicesError(deviceMac: String?, errorMessage: String?) {
        if (device == null) {
            ToastUtils.showLong("bluebooth is null")
            return
        }

        closeDevice()
        ToastUtils.showLong("bluetooth_search_service_failed")
    }

    override fun onConnectTimeout(deviceMac: String?) {
        hideLoading()
    }


    private fun showChoiceDialog(unbindDevice: FreeFitDevice?) {
        if (unbindDevice == null || !unbindDevice.isConnected) {
            ToastUtils.showLong("device_not_connect")
            return
        }
        val dialog: ChoiceDialog =
            ChoiceDialog.Builder(this).setTitle("unbind_device_title").setContent("").setDefine("unbind_device_btn_unbind").setVisibilityTitle(true).setListener {
                if (unbindDevice != null) {
                    LogUtils.e(TAG, "Device unbindDevice  DeviceMode :" + unbindDevice.getDeviceMode())
                    //
                    //首先解除系统配对
//                        String bleMac = unbindDevice.getDeviceMac();
//                        String btMac;
//
//                        byte uu = (byte) 0x55;
//                        String substring = bleMac.substring(15, 17);
//                        String bt1 = bleMac.substring(0, 15);
//                        int x = Integer.parseInt(substring, 16);
//                        String s = ByteUtil.bytesToHex1((byte) (x ^ uu));
//                        btMac = (bt1 + s).toUpperCase(Locale.ROOT);
//                        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
//                        BluetoothDevice remoteDevice = defaultAdapter.getRemoteDevice(btMac);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                            BtUtils.getInstance().cancelPinBule(remoteDevice);
//                        } else {
//                            BtUtils.getInstance().disConnectA2dp(remoteDevice);
//                        }
                    FirBluetoothManager.getInstance().remove(unbindDevice.deviceMac)
                    FirBluetoothManager.getInstance().disconnectAllDevice()
                    Hawk.delete(ApiConstant.DEVICE_MODE)
                    Hawk.delete(ApiConstant.DEVICE)
                    Hawk.delete(ApiConstant.DEVICE_NAME)
                    //用户手动解绑设备,将用户的设备信息 置空, 下次登录时显示去搜索设备的页面.
                    Hawk.put(ApiConstant.USER_DEVICE_INFO, "")
                    ToastUtils.showLong("device_disconnected")
                    finish()
                }
            }.build()
        dialog.show()
    }


    private fun closeDevice() {
        hideLoading()
        // 移除设备, 不再管理
        if (device != null) {
            FirBluetoothManager.getInstance().remove(device!!.deviceMac)
            BluetoothHelper.getInstance().disconnectDevice2(device!!.bluetoothDevice)
            BluetoothHelper.getInstance().destroy()
        }
        device = null
    }

    var findMac: String? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 22 && resultCode == 3) {
            findMac = data!!.getStringExtra("findMac")!!.uppercase(Locale.getDefault())
            if (macList.size == 0) {
                checkPermission()
            } else {
                if (macList.contains(findMac)) {
                    for (i in mList.indices) {
                        if (mList[i].device?.address?.toUpperCase().equals(findMac)) {
                            val scanDeviceBean: ScanDeviceBean = mList[i]
                            bindDevice(scanDeviceBean, scanDeviceBean.device)
                            break
                        }
                    }
                } else {
                    checkPermission()
                }
            }
        }
    }


    override fun onDestroy() {
        bluetoothScanner?.stopScan()
        FirBluetoothManager.getInstance().cancelScan()
        hideLoading()
        super.onDestroy()
    }



}