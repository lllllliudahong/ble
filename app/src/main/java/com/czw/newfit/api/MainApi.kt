package com.czw.newfit.api

import com.czw.newfit.bean.AuthCodeBean
import com.pingerx.rxnetgo.rxcache.CacheMode
import io.reactivex.disposables.Disposable


object MainApi : BaseApi {
    private const val SEND_CODE = "/api/send_code"


    /**
     * 获取验证码
     */
    fun getAuthCode(phone: String, sms_type: String, function: ApiSubscriber<AuthCodeBean>.() -> Unit): Disposable {
        val subscriber = object : ApiSubscriber<AuthCodeBean>() {}
        subscriber.function()
        return getApi()
            .get<AuthCodeBean>(SEND_CODE)
            .params("phone",phone)
            .params("sms_type",sms_type)
            .cacheMode(CacheMode.NONE)
            .subscribeWith(subscriber)
    }
}
