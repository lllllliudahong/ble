package com.czw.newfit.ui.health

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.czw.bluetoothlib.core.FirBluetoothManager
import com.czw.newfit.R
import com.czw.newfit.adapter.FunctionItemViewAdapter
import com.czw.newfit.api.ApiConstant
import com.czw.newfit.base.BoxFragment
import com.czw.newfit.bean.onBatteryNum
import com.czw.newfit.databinding.FragmentDeviceBinding
import com.czw.newfit.device.FreeFitDevice
import com.czw.newfit.utils.LaunchUtils
import com.miekir.common.extension.lazy
import com.orhanobut.hawk.Hawk
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeviceFragment : BoxFragment<FragmentDeviceBinding>() {

    private val mPresenter by lazy<DeviceFragment, DeviceFragmentPresenter>()

    override fun onBindingInflate() = FragmentDeviceBinding.inflate(layoutInflater)

    private var device: FreeFitDevice? = null
    private val topicList = ArrayList<String>()
    private var topicAdapter: FunctionItemViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onLazyInit() {
        binding.clTopDisDevice.setOnClickListener { LaunchUtils.startSearchDeviceActivity(this@DeviceFragment.activity) }
        binding.clTopContDevice.setOnClickListener { LaunchUtils.startSearchDeviceActivity(this@DeviceFragment.activity) }
        topicList.add("消息通知")
        topicList.add("天气推送")
        topicList.add("闹钟")
        topicList.add("遥控拍照")
        topicList.add("查找手环")
        topicList.add("同步通讯录")
        topicList.add("同步音乐")
        topicList.add("手表使用指南")
        topicList.add("更多设置")
        topicList.add("固件升级")

        binding.rvTopic.layoutManager = GridLayoutManager(context, 2)
        topicAdapter = FunctionItemViewAdapter(topicList)
        binding.rvTopic.adapter = topicAdapter
        topicAdapter?.setOnItemClickListener { adapter, view, position ->
            if (judgeConnectStatus()){

            } else{
                ToastUtils.showLong("设备未连接")
            }
        }

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (judgeConnectStatus()) {
            setContDeviceViews()
            topicAdapter?.setState(true)
        } else {
            setDisDeviceViews()
            topicAdapter?.setState(false)
        }
        topicAdapter?.notifyDataSetChanged()
    }


    /**
     * 显示已连接状态
     */
    private fun setContDeviceViews(){
        binding.clTopDisDevice.visibility = View.GONE
        binding.clTopContDevice.visibility = View.VISIBLE
        binding.tvDeviceName.text = device?.name
    }

    /**
     * 显示未连接状态
     */
    private fun setDisDeviceViews(){
        binding.clTopDisDevice.visibility = View.VISIBLE
        binding.clTopContDevice.visibility = View.GONE
    }


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
            device = b as FreeFitDevice
        }

        return device != null && device?.isConnected == true
    }


    /**
     * 电量
     */
    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBatteryNum(bean: onBatteryNum) {
        if (isAdded) {
            val battery = bean.battery
            requireActivity().runOnUiThread { //Fixme 空指针?
                if (isAdded) {
                    binding.ivBattery.visibility = View.VISIBLE
                    binding.tvBattery.visibility = View.VISIBLE
                    if (battery < 20) {
                        binding.ivBattery.setImageResource(R.mipmap.home_batery_1)
                    } else if (battery in 20..39) {
                        binding.ivBattery.setImageResource(R.mipmap.home_batery_2)
                    } else if (battery in 40..59) {
                        binding.ivBattery.setImageResource(R.mipmap.home_batery_3)
                    } else if (battery in 60..79) {
                        binding.ivBattery.setImageResource(R.mipmap.home_batery_4)
                    } else {
                        binding.ivBattery.setImageResource(R.mipmap.home_batery_5)
                    }
                    binding.tvBattery.text = "$battery%"
                }
            }
        }
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}