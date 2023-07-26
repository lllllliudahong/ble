package com.czw.newfit.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.czw.newfit.application.MainApplication
import java.io.File
import java.util.*


/**
 * App级别的工具类，提供系统的Context和常用的工具类
 */
object AppUtils {

    /**
     *  全局的Handler，发出了消息一定要自己手动关闭掉
     */
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private val mActivityList = LinkedList<Activity>()

    /**
     * 获取全局的Application
     * @return Application
     */
    fun getApplication(): Application {
        return MainApplication.getApplication()
    }

    /**
     * 获取全局的Context
     * @return Context
     */
    fun getContext(): Context {
        return getApplication().applicationContext
    }

    /**
     * 获取String资源集合
     */
    fun getString(@StringRes id: Int): String {
        return getContext().resources?.getString(id) ?: ""
    }

    /**
     * 获取color资源
     */
    fun getColor(@ColorRes color: Int): Int {
        return getContext().resources?.getColor(color) ?: 0
    }

    fun getDimension(@DimenRes id: Int): Float? {
        return getContext().resources?.getDimension(id)
    }

    fun getDimensionX(@DimenRes id: Int): Float {
        getContext().resources?.getDimension(id)?.let { return it }
        return 0F
    }

    /**
     * 获取Drawable对象
     */
    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(getContext(), id)
    }

    /**
     * 获取String资源集合
     */
    fun getStringArray(@ArrayRes id: Int): Array<String> {
        return getContext().resources.getStringArray(id)
    }

    /**
     * 获取打包渠道
     * 使用该方法需要在manifest中配置CHANNEL的meta_data
     */
    fun getChannel(): String {
        val appInfo: ApplicationInfo =
                getContext().packageManager.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA)
        if(appInfo.metaData.get("UMENG_CHANNEL") == null){
            return "default"
        }
        return appInfo.metaData.get("UMENG_CHANNEL")!!.toString()
    }

    /**
     * 获取版本号
     */
    @Suppress("DEPRECATION")
    fun getVersionCode(): Int {
        return getPackageInfo().versionCode
    }

    /**
     * 获取版本名
     */
    fun getVersionName(): String {
        return getPackageInfo().versionName
    }

    /**
     * 获取应用程序名称
     */
    fun getAppName(): String {
        val labelRes = getPackageInfo().applicationInfo.labelRes
        return getContext().resources.getString(labelRes)
    }


    /**
     * 获取当前应用的包名
     */
    fun getPackageName(): String {
        return getContext().packageName
    }


    /**
     * 获取包信息
     */
    fun getPackageInfo(): PackageInfo {
        return getContext().packageManager.getPackageInfo(getContext().packageName, 0)
    }

    /**
     * 手机设置厂商
     */
    fun getDeviceModel(): String {
        return Build.MODEL
    }

    /**
     * @param slotId  slotId为卡槽Id，它的值为 0、1；
     * @return
     */
    fun getIMEI(slotId: Int): String {
        return try {
            val manager = getContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val method = manager.javaClass.getMethod("getImei", Int::class.javaPrimitiveType!!)
            method.invoke(manager, slotId) as String
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 手机系统版本号，Android 6.0
     */
    fun getOsVersion(): String {
        return android.os.Build.VERSION.RELEASE
    }

    /**
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     */
    fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.data =
                Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            getContext().startActivity(intent)
            true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
    }

    /**
     * 将activity 移除
     */
    fun removeTopActivity(activity: Activity) {
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity)
        }
    }

    /**
     * 设置栈顶Activity
     */
    fun setTopActivity(activity: Activity) {
        if (mActivityList.contains(activity)) {
            if (mActivityList.last != activity) {
                mActivityList.remove(activity)
                mActivityList.addLast(activity)
            }
        } else {
            mActivityList.addLast(activity)
        }
    }

    /**
     * 获取所有启动过的Activity
     */
    fun getActivityList(): LinkedList<Activity> {
        return mActivityList
    }

    /**
     * 判断当前APP是否在前台运行
     */
    fun isAppForeground(): Boolean {
        val activityManager = getContext().getSystemService(
                Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            if (appProcess.processName == getPackageName() && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }


    /**
     * 获取当前应用的信息
     */
    fun getAppInfo(): AppInfo? = getAppInfo(getContext().packageName)


    /**
     * 获取指定包名的应用信息
     * @param packageName 包名
     */
    fun getAppInfo(packageName: String): AppInfo? {
        return try {
            val pm = getContext().packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            getAppInfo(pm, pi)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }

    }

    /**
     * 获取所有的应用信息
     */
    fun getAppsInfo(): List<AppInfo> {
        val list = ArrayList<AppInfo>()
        val pm = getContext().packageManager
        val installedPackages = pm.getInstalledPackages(0)
        for (pi in installedPackages) {
            val ai = getAppInfo(pm, pi) ?: continue
            list.add(ai)
        }
        return list
    }

    @Suppress("DEPRECATION")
    private fun getAppInfo(pm: PackageManager?, pi: PackageInfo?): AppInfo? {
        if (pm == null || pi == null) return null
        val ai = pi.applicationInfo
        val packageName = pi.packageName
        val name = ai.loadLabel(pm).toString()
        val icon = ai.loadIcon(pm)
        val packagePath = ai.sourceDir
        val versionName = pi.versionName
        val versionCode = pi.versionCode
        val isSystem = ApplicationInfo.FLAG_SYSTEM and ai.flags != 0
        return AppInfo(packageName, name, icon, packagePath, versionName, versionCode, isSystem)
    }

    private fun isFileExists(file: File?): Boolean {
        return file != null && file.exists()
    }

    private fun getFileByPath(filePath: String): File? {
        return if (isSpace(filePath)) null else File(filePath)
    }

    private fun isSpace(s: String?): Boolean {
        if (s == null) return true
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    private fun isDeviceRooted(): Boolean {
        val su = "su"
        val locations = arrayOf(
                "/system/bin/",
                "/system/xbin/",
                "/sbin/",
                "/system/sd/xbin/",
                "/system/bin/failsafe/",
                "/data/local/xbin/",
                "/data/local/bin/",
                "/data/local/"
        )
        for (location in locations) {
            if (File(location + su).exists()) {
                return true
            }
        }
        return false
    }

    /**
     * The application's information.
     */
    class AppInfo(
            packageName: String, name: String, icon: Drawable, packagePath: String,
            versionName: String, versionCode: Int, isSystem: Boolean
    ) {

        var packageName: String? = null
        var name: String? = null
        var icon: Drawable? = null
        var packagePath: String? = null
        var versionName: String? = null
        var versionCode: Int = 0
        var isSystem: Boolean = false

        init {
            this.name = name
            this.icon = icon
            this.packageName = packageName
            this.packagePath = packagePath
            this.versionName = versionName
            this.versionCode = versionCode
            this.isSystem = isSystem
        }

        override fun toString(): String {
            return "pkg name: " + packageName +
                    "\napp icon: " + icon +
                    "\napp name: " + name +
                    "\napp path: " + packagePath +
                    "\napp v name: " + versionName +
                    "\napp v code: " + versionCode +
                    "\nis system: " + isSystem
        }
    }

}