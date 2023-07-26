package com.czw.newfit.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.czw.bluetoothlib.core.FirBluetoothManager
import com.czw.newfit.R
import com.czw.newfit.adapter.TabListAdapter
import com.czw.newfit.adapter.ViewPagerAdapter
import com.czw.newfit.api.ApiConstant
import com.czw.newfit.base.BoxActivity
import com.czw.newfit.bean.MainTabItemBean
import com.czw.newfit.bean.onActivityResultBean
import com.czw.newfit.databinding.ActivityMainBinding
import com.czw.newfit.device.FreeFitDevice
import com.czw.newfit.receiver.CallReceiver
import com.czw.newfit.service.NotificationMonitor
import com.czw.newfit.ui.health.DeviceFragment
import com.czw.newfit.ui.home.HomeFragment
import com.czw.newfit.ui.me.MeFragment
import com.czw.newfit.ui.sport.SportFragment
import com.czw.newfit.utils.LayoutManagerUtil
import com.czw.newfit.utils.NotificationUtils
import com.czw.newfit.widget.dialog.DialogUtils
import com.miekir.common.extension.lazy
import com.orhanobut.hawk.Hawk
import org.greenrobot.eventbus.EventBus


class MainActivity : BoxActivity<ActivityMainBinding>(), OnPageChangeListener {
    private val TAG = "HomeFragment"
    private val mPresenter by lazy<MainActivity, MainPresenter>()

    override fun onBindingInflate() = ActivityMainBinding.inflate(layoutInflater)

    private val fragments = arrayListOf<Fragment>()
    private var viewPagerAdapter: ViewPagerAdapter? = null

    private var tabListdata = arrayListOf<MainTabItemBean>()
    var tabAdapter: TabListAdapter? = null

    private var callReceiver: CallReceiver? = null


    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                if (BluetoothAdapter.STATE_ON == state) { // 蓝牙开
                    val deviceMac = Hawk.get<String>(ApiConstant.DEVICE)
                    Log.e(TAG, "--蓝牙开--lastMac:--deviceMac: $deviceMac")
                    if (deviceMac != null) {
                        val b = FirBluetoothManager.getInstance().getDevice(deviceMac)
                        var freeFitDevice: FreeFitDevice? = null
                        if (b != null){
                            freeFitDevice = b as FreeFitDevice
                            Log.e(TAG, "--蓝牙开--lastMac:--freeFitDevice: $freeFitDevice")
                            FirBluetoothManager.getInstance().connect(freeFitDevice)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onInit() {


        if (!isEnabled()) {
            showDialog()
        }
        toggleNotificationListenerService()

        //蓝牙状态监听
        val intent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiver, intent)

        val panelFilter = IntentFilter()
        //添加监听电话状态变化的Action
        panelFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        //设置咱们这个广播的优先级
        panelFilter.priority = Int.MAX_VALUE
        //注册
        callReceiver = CallReceiver()
        registerReceiver(callReceiver, panelFilter)

        //主页
        fragments.add(HomeFragment())
        tabListdata.add(MainTabItemBean(R.mipmap.tabar_home_sel, R.mipmap.tabar_home_nor,"首页", true))
        fragments.add(DeviceFragment())
        tabListdata.add(MainTabItemBean(R.mipmap.tabar_device_sel, R.mipmap.tabar_device_nor,"设备", false))
        fragments.add(SportFragment())
        tabListdata.add(MainTabItemBean(R.mipmap.tabar_sports_sel, R.mipmap.tabar_sports_nor,"运动", false))
        fragments.add(MeFragment())
        tabListdata.add(MainTabItemBean(R.mipmap.tabar_my_sel, R.mipmap.tabar_my_nor,"我的", false))

        //内容
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, fragments)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.setCurrentItem(0, false)
        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.addOnPageChangeListener(this)

        //tab
        tabAdapter = TabListAdapter(tabListdata)
        binding.tabList.layoutManager = LayoutManagerUtil.getGridLayoutManager(this, fragments.size)
        binding.tabList.adapter = tabAdapter
        tabAdapter?.setList(tabListdata)

        tabAdapter?.setOnItemClickListener { adapter, view, position ->
            if (position < fragments.size) {
                tabAdapter?.data?.let {
                    it.forEach { item ->
                        item.isSelect = false
                    }
                }
                tabAdapter?.getItem(position)?.isSelect = true
                tabAdapter?.notifyDataSetChanged()
                binding.viewPager.setCurrentItem(position, false)
            }
        }


    }


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        binding.viewPager.setCurrentItem(position, false)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    /**
     * https://www.zhihu.com/question/33540416
     * 应用进程被杀后再次启动时，服务不生效（没有bindService）
     * 还原方法：重启手机
     *
     * 触发系统重新bind
     * disable再enable即可触发系统rebind操作。
     */
    private fun toggleNotificationListenerService() {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationMonitor::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationMonitor::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }


    private fun isEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun showDialog() {
        DialogUtils.showCenterDialog(this,
            "",
            "FereFit请求读取通知信息,用于推送信息给手环",
            false,
            "cancel",
            "confirm"
        ) { baseDialog, view ->
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            baseDialog.dismiss()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EventBus.getDefault().post(onActivityResultBean(requestCode))
    }

    override fun onDestroy() {
        //        Hawk.put(AppConstants.APP_KILL, true);
        NotificationUtils.getInstance().cancelNotification()
        unregisterReceiver(receiver)
        unregisterReceiver(callReceiver)
        super.onDestroy()
    }

}