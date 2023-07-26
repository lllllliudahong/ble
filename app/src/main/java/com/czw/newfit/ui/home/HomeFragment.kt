package com.czw.newfit.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.czw.bluetoothlib.core.FirBluetoothManager
import com.czw.bluetoothlib.listener.BluetoothDeviceStateListener
import com.czw.newfit.adapter.TopItemView2Adapter
import com.czw.newfit.adapter.TopItemViewAdapter
import com.czw.newfit.api.ApiConstant
import com.czw.newfit.base.BoxFragment
import com.czw.newfit.bean.FreeFitSupportFunction
import com.czw.newfit.bean.RealTimeSteps
import com.czw.newfit.bean.onActivityResultBean
import com.czw.newfit.bean.onDeviceStateChange
import com.czw.newfit.databinding.FragmentHomeBinding
import com.czw.newfit.device.FreeFitDevice
import com.czw.newfit.utils.LaunchUtils
import com.czw.newfit.utils.LogUtils
import com.miekir.common.extension.lazy
import com.orhanobut.hawk.Hawk
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : BoxFragment<FragmentHomeBinding>(), BluetoothDeviceStateListener, FreeFitDevice.DataCallback {

    private val mPresenter by lazy<HomeFragment, HomeFragmentPresenter>()

    override fun onBindingInflate() = FragmentHomeBinding.inflate(layoutInflater)

    private var topicAdapter1: TopItemViewAdapter? = null
    private var topicAdapter2: TopItemView2Adapter? = null
    private val topicBeanListAll= ArrayList<String>()
    private val topicBeanList1= ArrayList<String>()
    private val topicBeanList2= ArrayList<String>()

    private val TAG = "HomeFragment"

    private var freeFitDevice: FreeFitDevice? = null

    /**
     * 判断设备的连接状态
     *
     * @return
     */
    private fun judgeConnectStatus(): Boolean {
        val deviceMac = Hawk.get<String>(ApiConstant.DEVICE)
        if (TextUtils.isEmpty(deviceMac)) {
            return false
        }
        val b = FirBluetoothManager.getInstance().getDevice(deviceMac)
        if (b != null) {
            freeFitDevice = b as FreeFitDevice
        }

        return freeFitDevice != null && freeFitDevice?.isConnected == true
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onLazyInit() {
        EventBus.getDefault().register(this)

        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")

        topicBeanList1.clear()
        topicBeanList2.clear()
        if (topicBeanListAll.size > 4){
            topicBeanList1.addAll(topicBeanListAll.subList(0, 4))
            topicBeanList2.addAll(topicBeanListAll.subList(4, topicBeanListAll.size))
        }else{
            topicBeanList1.addAll(topicBeanListAll)
        }

        binding.rvTopic1.layoutManager = GridLayoutManager(context, 2)
        topicAdapter1 = TopItemViewAdapter(topicBeanList1)
        binding.rvTopic1.adapter = topicAdapter1
        topicAdapter1?.setOnItemClickListener { adapter, view, position ->  }

        binding.rvTopic2.layoutManager = GridLayoutManager(context, 2)
        topicAdapter2 = TopItemView2Adapter(topicBeanList2)
        binding.rvTopic2.adapter = topicAdapter2
        topicAdapter2?.setOnItemClickListener { adapter, view, position ->  }

        binding.circular1.setProgressDisplayAndInvalidate(100)
        binding.circular1.setOnTouchListener { _, _ -> true }
        binding.circular2.setOnTouchListener { _, _ -> true }
        binding.circular3.setOnTouchListener { _, _ -> true }

        binding.tvMore.setOnClickListener {
            LaunchUtils.startMoreTopicActivity(activity)
        }
        //已连接，第一次进来，直接同步数据
        if (judgeConnectStatus()){
            getAllSupportFunctions()
        }


        //下拉刷新
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.setOnRefreshListener(OnRefreshListener { refreshLayout ->
            if (freeFitDevice != null) {
                if (freeFitDevice!!.isConnected) {
                    getAllSupportFunctions()
                }
            }
            Handler().postDelayed({
            }, 16000)
            refreshLayout.finishRefresh(16000, true, false)
        })
    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        // 查询上次是否有连接过设备
        val userDeviceInfo = Hawk.get(ApiConstant.USER_DEVICE_INFO, "")
        if (!TextUtils.isEmpty(userDeviceInfo)) {
            val split = userDeviceInfo.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (split.size >= 2) {
                val deviceMac = split[1]
                LogUtils.e(TAG, "current user is   last bind device is $deviceMac  --- freeFitDevice = $freeFitDevice")
                if (null != freeFitDevice) {
                    if (freeFitDevice!!.isConnected) {
                        binding.tvDeviceState.visibility = View.VISIBLE
                        binding.tvDeviceState.text = "已连接" + freeFitDevice?.name
                    } else {
                        processConnectedDevice(deviceMac)
                    }
                } else {
                    processConnectedDevice(deviceMac)
                }
            }
        }else{
            binding.tvDeviceState.visibility = View.GONE
        }
    }


    @SuppressLint("SetTextI18n")
    private fun processConnectedDevice(deviceMac: String) {
        LogUtils.e(TAG, "processConnectedDevice---Device mac:$deviceMac")
        val b = FirBluetoothManager.getInstance().getDevice(deviceMac)
        if(b != null){
            freeFitDevice = b as FreeFitDevice
        }
        // 获取不到本地管理的设备, 可能是重启app, 则重新创建管理
        if (null == freeFitDevice) {
            freeFitDevice = FreeFitDevice()
            val deviceName = Hawk.get(ApiConstant.DEVICE_NAME, "")
            LogUtils.e(TAG, "展示连接信息$deviceName")
            binding.tvDeviceState.visibility = View.VISIBLE
            binding.tvDeviceState.text = "断开连接$deviceName"
            freeFitDevice?.bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceMac)
            FirBluetoothManager.getInstance().connect(freeFitDevice)
            freeFitDevice?.removeBluetoothEventListener()
            freeFitDevice?.addBluetoothEventListener()
            freeFitDevice?.removeDeviceStateListener(this)
            freeFitDevice?.addDeviceStateListener(this)
        } else {
            freeFitDevice?.removeBluetoothEventListener()
            freeFitDevice?.addBluetoothEventListener()
            freeFitDevice?.removeDeviceStateListener(this)
            freeFitDevice?.addDeviceStateListener(this)
            refreshLayoutWithDevice()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshLayoutWithDevice() {
        if (freeFitDevice!!.isConnected) {
            LogUtils.e(TAG, "refreshLayoutWithDevice---Device is connected.")
            binding.tvDeviceState.text = "已连接" + freeFitDevice?.name
            //延时同步数据
            Handler(Looper.getMainLooper()).postDelayed({ getAllSupportFunctions() }, 1500)
        } else {
            LogUtils.e(TAG, "Device is disconnect.")
            binding.tvDeviceState.text = "已断开"
            FirBluetoothManager.getInstance().connect(freeFitDevice)
        }
    }

    /**
     * 以下四个回调是当前页面重连的状态
     */
    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onStateChange(device: BluetoothDevice?, oldState: Int, newState: Int) {
        LogUtils.e(
            TAG,
            "Device state onStateChange --onStateChange-- newState: $newState" + (if (BluetoothProfile.STATE_CONNECTED == newState) "连接" else "断开") + "--mac:" + if (device == null) "" else device.address
        )
        if (isAdded){
            if (BluetoothProfile.STATE_CONNECTED == newState) {
                Hawk.put(ApiConstant.DEVICE, device!!.address)
                binding.tvDeviceState.text = "已连接" + device.name
                Handler(Looper.getMainLooper()).postDelayed({
                    getAllSupportFunctions()//延时同步数据，防止刚连接，发送消息还未初始化
                }, 1500)

            }else{
                binding.tvDeviceState.text = "已断开" + Hawk.get(ApiConstant.DEVICE_NAME)
            }
        }

    }

    override fun onReady(deviceMac: String?) {
    }

    override fun onDiscoverServicesError(deviceMac: String?, errorMessage: String?) {
    }

    override fun onConnectTimeout(deviceMac: String?) {
    }


    /**
     * 同步数据(间隔15秒)
     */
    private var lastAllFunTime = 0L
    private fun getAllSupportFunctions() {
        Log.e(TAG, "---开始同步数据getAllSupportFunctions.")
        if (System.currentTimeMillis() - lastAllFunTime > 15 * 1000) {
            lastAllFunTime = System.currentTimeMillis()
            if (freeFitDevice != null) {
                freeFitDevice!!.setDataCallback(this)
                //                showLoading(getResources().getString(R.string.sync_data_wait_a_moment));
                freeFitDevice!!.getAllSupportFunctions()
//                freeFitDevice!!.switchUnit(Hawk.get(ApiConstant.IS_METRIC, false)) //同步单位
            }
        }else{
            Log.e(TAG, "---小于15秒，停止这次同步数据getAllSupportFunctions.")
        }
    }

    /**
     * 从搜索页面连接成功，收到消息
     */
    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onDeviceStateChange(state: onDeviceStateChange) {
        LogUtils.e(TAG,
            "Device state onStateChange --onDeviceStateChange-- newState:" + (if (BluetoothProfile.STATE_CONNECTED == state.status) "连接" else "断开") + "--mac:" + if (state.device == null) "" else state.device?.address
        )
        if (BluetoothProfile.STATE_CONNECTED == state.status) {
            freeFitDevice = FirBluetoothManager.getInstance().getDevice(state.device?.address) as FreeFitDevice
            getAllSupportFunctions()//同步数据
            binding.tvDeviceState.visibility = View.VISIBLE
            binding.tvDeviceState.text = "已连接" + Hawk.get(ApiConstant.DEVICE_NAME, "")
        } else {
            binding.tvDeviceState.visibility = View.GONE
        }
    }

    /**
     * 实时步数，卡路里，距离
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRealTimeSteps(realTimeSteps: RealTimeSteps) {
        val freeFitStepsBean = realTimeSteps.freeFitStepsBean
        LogUtils.e(TAG, "onRealTimeSteps : $freeFitStepsBean")
//        setStepNum(false)
    }

    /**
     * 编辑卡片返回，更新首页卡片
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onActivityResult(bean: onActivityResultBean) {
        if (bean.requestCode == LaunchUtils.GOTO_MORE_TOPIC){
            ToastUtils.showLong("编辑卡片返回，更新首页卡片")
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onFindBandResult(isSuccess: Boolean, status: Int) {
        TODO("Not yet implemented")
    }

    override fun onRequestGPSAddress() {
        TODO("Not yet implemented")
    }

    /**
     * 设备支持的类型
     */
    override fun onFunctionList(freeFitSupportFunction: FreeFitSupportFunction?) {
        freeFitDevice?.queryBattery()
        if (freeFitSupportFunction != null) {
            Log.e(TAG, "onFunctionList  freeFitSupportFunction:$freeFitSupportFunction")
//            Handler(Looper.getMainLooper()).postDelayed({
//                isSupportHeartRate = freeFitSupportFunction.supportHeartRate
//                isSupportBloodPressure = freeFitSupportFunction.supportBloodPressure
//                isSupportBloodOxygen = freeFitSupportFunction.supportBloodOxygen
//                isSupportECG = freeFitSupportFunction.supportECG
//                isSupportBloodSugar = freeFitSupportFunction.supportBloodSugar
//                isSupportSleep = freeFitSupportFunction.supportSleep
//                isSupportTemperature = freeFitSupportFunction.supportTemperature
//                isSupportMultiSport = freeFitSupportFunction.supportMultiSport
//                //  连接的设备类型
//                freeFitDevice!!.setDeviceMode(freeFitSupportFunction.firmwarePlate)
//                //                Log.e("liuhong", " freeFitSupportFunction.getFirmwarePlate() = " + freeFitSupportFunction.getFirmwarePlate());
//                SPUtils.setDeviceMode(freeFitSupportFunction.firmwarePlate)
//                Hawk.put(AppConstants.DEVICE_MODE, freeFitSupportFunction.firmwarePlate)
//                setTopicData()
//                syncTimeFun()
//            }, 500)
        }
    }

    override fun onRealTimeStepSwitch(isOn: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onVersionInfo(versionInfo: String?) {
        TODO("Not yet implemented")
    }

    override fun onDeviceAntiLost(isOn: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onInfoData(index: Byte, status: Byte) {
        TODO("Not yet implemented")
    }

    override fun onAnswerWeather(b: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onSyncSportRecordOver() {
        TODO("Not yet implemented")
    }

}