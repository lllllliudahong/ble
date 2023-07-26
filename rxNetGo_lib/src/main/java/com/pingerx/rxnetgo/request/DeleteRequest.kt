package com.pingerx.rxnetgo.request

import com.pingerx.rxnetgo.request.base.ApiService
import com.pingerx.rxnetgo.request.base.BodyRequest
import io.reactivex.Flowable
import okhttp3.RequestBody

class DeleteRequest<T>(url: String, service: ApiService?, flowable: Flowable<T>?) : BodyRequest<T>(url, service, flowable) {

/*    override fun generateRequestBody(): RequestBody? {
        return
    }*/
    override fun getMethod(): RequestType {
        return RequestType.DELETE
    }
}