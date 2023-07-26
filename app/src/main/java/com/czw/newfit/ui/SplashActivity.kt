package com.czw.newfit.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.czw.newfit.R
import com.czw.newfit.utils.LaunchUtils
import com.czw.newfit.utils.NotificationUtils
import com.czw.newfit.utils.PermissionUtil
import com.czw.newfit.utils.StatusBarUtil
import com.czw.newfit.widget.dialog.PermssionDialog
import com.permissionx.guolindev.PermissionX

@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //设置沉浸式状态栏
        StatusBarUtil.setTranslucentStatus(this)
        //设置状态栏字体颜色
        StatusBarUtil.setStatusBarForegroundColor(this, true)
        super.onCreate(savedInstanceState)
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
        setContentView(R.layout.activity_splash)

//        if (LoginOrdinaryUtils.isLoginState()) {
//            LaunchUtils.startActivity(this, MainActivity::class.java)
//        }else {
//            LaunchUtils.startActivity(this, LoginRegisterActivity::class.java)
//        }
        NotificationUtils.getInstance().startNotification(this@SplashActivity, MainActivity::class.java)
        LaunchUtils.startActivity(this@SplashActivity, MainActivity::class.java);
        finish()

    }

    private fun checkPermission() {
        var required_permission_list2: Array<String> = PermissionUtil.getInstance().REQUIRED_PERMISSION_LIST2
        required_permission_list2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionUtil.getInstance().REQUIRED_PERMISSION_LIST2
        } else {
            PermissionUtil.getInstance().REQUIRED_PERMISSION_LIST2_1
        }
        PermissionX.init(this@SplashActivity)
            .permissions(permissions = required_permission_list2)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    //初始化第三方的一些东西
//                    SDKInitUtil.getInstance().Init(App.getInstance())
                    NotificationUtils.getInstance().startNotification(this@SplashActivity, MainActivity::class.java)
                    LaunchUtils.startActivity(this@SplashActivity, MainActivity::class.java);
                    finish()
                } else {
                    permissionDialog("允许应用程序获取Wi-Fi网络状态的权限，以便能顺利登录账户。\n" +
                            "\\n允许程序读写权限，以便保存您登录后的用户数据。\n" +
                            "\\n允许程序定位权限，以便保存您的账户登录安全。", 0)
                }
        }
    }

    private fun permissionDialog(str: String?, type: Int) {
        val commonDialog = PermssionDialog(this)
        commonDialog.setContent(str)
        commonDialog.setOnItemClickListener { position ->
            if (type == 0) {
                toAppSetting()
            } else {
                checkPermission()
            }
        }
        commonDialog.show()
    }

    private fun toAppSetting() {
        val intent = Intent()
        //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", packageName, null)
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra("com.android.settings.ApplicationPkgName", packageName)
        }
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            checkPermission()
        }
    }
}
