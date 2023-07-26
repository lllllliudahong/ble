package com.czw.newfit.api

import android.os.Build
import com.pingerx.rxnetgo.RxNetGo
import com.pingerx.rxnetgo.model.HttpHeaders
import java.lang.StringBuilder

/**
 *
 * 网络请求基类
 */
interface BaseApi {

    /**
     * 获取URL
     */
    fun getBaseUrl(): String {
        return ApiConstant.API_URL
    }

    fun getToken(): String? {
//        return PreferencesHelper.getPref("token", "")
        return ""
    }

    /**
     * 获取默认的Service
     * 需要子类绑定URL
     */
    fun getApi(): RxNetGo {
//        val token = getToken()
//        return if (token != null && token != "") {
//            addHeaders(RxNetGo.getInstance().getRetrofitService(getBaseUrl()),"token=$token")
//        } else {
//            addHeaders(RxNetGo.getInstance().getRetrofitService(getBaseUrl()),"")
//        }
        return RxNetGo.getInstance().getRetrofitService(getBaseUrl())
    }

    private fun addHeaders(netGo: RxNetGo, token: String): RxNetGo {
        val user = StringBuilder()
//        user.append("otcnum/" + AppUtils.getVersionName() + " ")
        user.append("(")
        user.append(Build.MODEL)
        user.append(";")
        user.append("android ${Build.VERSION.RELEASE}")
        user.append(";")
        user.append(")")
//        user.append(" channel/" + AppUtils.getChannel())
        netGo.addCommonHeaders("public", HttpHeaders("Authorization", token))
//                .addCommonHeaders("user", HttpHeaders(HttpHeaders.HEAD_KEY_USER_AGENT, user.toString()))
        return netGo
    }
}