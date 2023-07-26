package com.pingerx.rxnetgo.request

import com.pingerx.rxnetgo.request.base.ApiService
import com.pingerx.rxnetgo.request.base.BodyRequest
import io.reactivex.Flowable

class PutRequest<T>(url: String, service: ApiService?, flowable: Flowable<T>?) : BodyRequest<T>(url, service, flowable) {

    override fun getMethod(): RequestType {
        return RequestType.PUT
    }
}