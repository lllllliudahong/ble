package com.czw.newfit.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.czw.newfit.ui.home.MoreTopicActivity
import com.czw.newfit.ui.me.ModifyInfoActivity
import com.czw.newfit.ui.search.SearchDeviceActivity

/**
 *
 * 页面跳转的工具类
 */
object LaunchUtils {

    const val GOTO_MORE_TOPIC = 2000//跳转到编辑卡片页面

    fun startActivity(context: Context?, clazz: Class<*>) {
        context?.startActivity(Intent(context, clazz))
    }

    fun startActivity(context: Context?, intent: Intent) {
        context?.startActivity(intent)
    }

    /**
     * 打开H5页面
     * "https://m1.sit.qiuhui.com/shop.html#/home?outer=0"
     */
//    fun startBrowserActivity(activity: Activity?, url: String) {
//        activity?.let {
//            val intent = Intent(it, BrowserActivity::class.java)
//            intent.putExtra(Constant.WEB_PARAM_URL, url)
//            intent.putExtra(Constant.WEB_PARAM_MODE, 0)
//            it.startActivityForResult(intent, -1)
//        }
//    }

    /**
     * 打开搜索设备页面
     */
    fun startSearchDeviceActivity(activity: Activity?) {
        activity?.let {
            val intent = Intent(it, SearchDeviceActivity::class.java)
            it.startActivity(intent)
        }
    }

    /**
     * 打开编辑卡片页面
     */
    fun startMoreTopicActivity(activity: Activity?) {
        activity?.let {
            val intent = Intent(it, MoreTopicActivity::class.java)
            it.startActivityForResult(intent, GOTO_MORE_TOPIC)
        }
    }

    /**
     * 打开个人资料页面
     */
    fun startModifyInfoActivity(activity: Activity?) {
        activity?.let {
            val intent = Intent(it, ModifyInfoActivity::class.java)
            it.startActivity(intent)
        }
    }

    /**
     * 打开浏览器
     */
    fun startBrowser(url: String?) {
        try {
            if (!TextUtils.isEmpty(url)) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                AppUtils.getContext().startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}