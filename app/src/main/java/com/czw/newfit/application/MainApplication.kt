package com.czw.newfit.application

import android.app.Application
import android.content.Context
import com.czw.bluetoothlib.app.BlueApplication
import com.czw.newfit.db.BaseManager
import com.czw.newfit.db.GreenDaoContext
import com.orhanobut.hawk.Hawk
import com.pingerx.rxnetgo.RxNetGo

class MainApplication : Application() {

    companion object {

        private lateinit var mApplication: MainApplication
        fun getApplication(): MainApplication {
            return mApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        mApplication = this
        BlueApplication.setApplication(mApplication)
        //基础数据保存框架
        Hawk.init(this).build()
        //初始化数据库
        BaseManager.initOpenHelper(
            GreenDaoContext(mApplication),
            "bracelet.db"
        )
        initNetWork()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // 初始化MultiDex 如果是android 5.0以上的设备，只需要设置为multiDexEnabled true
//        MultiDex.install(base)
    }

    private fun initNetWork() {
        RxNetGo.getInstance().init(this).debug(true)
        // 初始化Hybrid框架
//        if (!SonicEngine.isGetInstanceAllowed()) {
//            SonicEngine.createInstance(HostSonicRuntime(AppUtils.getApplication()), SonicConfig.Builder().build())
//        }
    }
}