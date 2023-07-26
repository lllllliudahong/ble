package com.pingerx.rxnetgo.interceptor

import com.pingerx.rxnetgo.RxNetGo.Companion.FILE_UPLOAD_MILLISECONDS
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

class DynamicTimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val questUrl: String = request.url().toString()
        val isFileUploadApi = questUrl.contains("api/user/upload")
        if (isFileUploadApi) {
            return chain.withConnectTimeout(FILE_UPLOAD_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .withReadTimeout(FILE_UPLOAD_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .withWriteTimeout(FILE_UPLOAD_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .proceed(request)
        }
        return chain.proceed(request)
    }

}