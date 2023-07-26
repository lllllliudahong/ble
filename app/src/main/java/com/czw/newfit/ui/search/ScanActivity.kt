package com.czw.newfit.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.czw.newfit.R
import com.czw.newfit.utils.StatusBarUtil
import com.czw.newfit.widget.LoadingDialog
import com.king.zxing.CaptureActivity
import com.king.zxing.camera.FrontLightMode
import com.miekir.common.extension.enableHighRefreshRate
import java.util.Locale
import java.util.regex.Pattern

/**
 * 搜索蓝牙页面
 */
class ScanActivity : CaptureActivity() {

    private val TAG = "ScanActivity"

    // 加载框
    private var mLoadingDialog: LoadingDialog? = null
    var findMac = "88:42:00:00:AA:55"

    override fun getLayoutId(): Int {
        return R.layout.custom_capture_activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        //设置沉浸式状态栏
        StatusBarUtil.setTranslucentStatus(this)
        //设置状态栏字体颜色
        StatusBarUtil.setStatusBarForegroundColor(this, true)
        // 启用高刷新率
        enableHighRefreshRate()
        mLoadingDialog = LoadingDialog(this)

        super.onCreate(savedInstanceState)

        mLoadingDialog = LoadingDialog(this)

        captureHelper.playBeep(false)
            .vibrate(true)
            .supportVerticalCode(true)
            .frontLightMode(FrontLightMode.AUTO)
            .tooDarkLux(45f)
            .brightEnoughLux(100f)
            .continuousScan(true)
            .supportLuminanceInvert(true)

        findViewById<View>(R.id.ivBack).setOnClickListener { v: View? -> finish() }

    }


    /**
     * 扫码结果回调
     *
     * @param result 扫码结果
     * @return
     */
    override fun onResultCallback(result: String): Boolean {
        if (result.uppercase(Locale.getDefault()).contains("MAC:")) {
            val strs = result.uppercase(Locale.getDefault()).split("MAC:".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            findMac = strs[1]
            if (isMacAddress(findMac)) {
//                startScan();
                //设置返回的数据
                val intent = Intent()
                intent.putExtra("findMac", findMac)
                intent.putExtra("deviceMode", 0)
                setResult(3, intent) //resultCode==3
                //关闭当前activity
                finish()
            } else {
                ToastUtils.showShort("error")
            }
        } else if (result.startsWith("http")) {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW" //Intent.ACTION_VIEW
            val content_url = Uri.parse(result)
            intent.data = content_url
            startActivity(intent)
        }
        return super.onResultCallback(result)
    }


    override fun onDestroy() {
        super.onDestroy()
        // 退出时隐藏loading框
        if (mLoadingDialog!!.isShowing) {
            mLoadingDialog!!.dismiss()
        }
        mLoadingDialog = null
    }

    fun showLoading(message: String?, isCancelable: Boolean) {
        runOnUiThread {
            Log.e("TAG", "isFinishing:" + isFinishing + ", isShowing:" + mLoadingDialog!!.isShowing)
            if (!isFinishing && !mLoadingDialog!!.isShowing) {
                mLoadingDialog!!.setContent(message)
                //                mLoadingDialog.setCancelable(isCancelable);
//                mLoadingDialog.setCancledOnTouchOutside(isCancelable);
                mLoadingDialog!!.show()
            }
        }
    }


    override fun onStop() {
        super.onStop()
    }


    private fun isMacAddress(macAddress: String): Boolean {
        val reg = "^([0-9a-fA-F]){2}([:][0-9a-fA-F]{2}){5}"
        return Pattern.compile(reg).matcher(macAddress).find()
    }

}