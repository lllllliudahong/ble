package com.czw.newfit.base

import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.KeyboardUtils
import com.czw.newfit.utils.StatusBarUtil
import com.czw.newfit.widget.LoadingDialog
import com.miekir.common.extension.enableHighRefreshRate
import com.miekir.mvp.view.binding.adapt.BindingActivity

/**
 * 基础Activity
 */
abstract class BoxActivity<VB : ViewBinding> : BindingActivity<VB>() {
    companion object {
        const val SIZE_IN_DP_WIDTH = 375.0f
    }

    override fun isBaseOnWidth(): Boolean {
        return true
    }

    override fun getSizeInDp(): Float {
        return SIZE_IN_DP_WIDTH
    }


    // 加载框
    private var mLoadingDialog: LoadingDialog? = null

    open fun showLoading(message: String?, isCancelable: Boolean) {
        runOnUiThread {
            Log.e("TAG", "isFinishing:$isFinishing" + ", isShowing:" + mLoadingDialog!!.isShowing)
            if (!isFinishing && !mLoadingDialog!!.isShowing) {
                mLoadingDialog!!.setContent(message)
                //                mLoadingDialog.setCancelable(isCancelable);
//                mLoadingDialog.setCancledOnTouchOutside(isCancelable);
                mLoadingDialog!!.show()
            }
        }
    }

    open fun showLoading(@StringRes strRes: Int, isCancelable: Boolean) {
        val message = resources.getString(strRes)
        showLoading(message, isCancelable)
    }

    open fun hideLoading() {
        if (mLoadingDialog != null) {
            runOnUiThread { mLoadingDialog!!.dismiss() }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        //设置沉浸式状态栏
        StatusBarUtil.setTranslucentStatus(this)
        //设置状态栏字体颜色
        StatusBarUtil.setStatusBarForegroundColor(this, true)
        touchSpaceHideKeyboard = true
        // 启用高刷新率
        enableHighRefreshRate()
        mLoadingDialog = LoadingDialog(this)
        super.onCreate(savedInstanceState)
    }

    override fun onPause() {
        // 必须要在onPause隐藏键盘，在onDestroy就太晚了
        KeyboardUtils.hideSoftInput(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        KeyboardUtils.fixSoftInputLeaks(this)
        // 退出时隐藏loading框
        if (mLoadingDialog!!.isShowing) {
            mLoadingDialog!!.dismiss()
        }
        mLoadingDialog = null
    }
}