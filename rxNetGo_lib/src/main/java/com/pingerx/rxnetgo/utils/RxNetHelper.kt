package com.pingerx.rxnetgo.utils

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import com.pingerx.rxnetgo.convert.base.IConverter
import com.pingerx.rxnetgo.exception.ApiException
import com.pingerx.rxnetgo.exception.NetErrorEngine
import com.pingerx.rxnetgo.request.RequestType
import com.pingerx.rxnetgo.request.base.Request
import com.pingerx.rxnetgo.rxcache.rxCache
import com.pingerx.rxnetgo.rxcache.stategy.ICacheStrategy
import com.pingerx.rxnetgo.subscribe.base.RxSubscriber
import com.pingerx.rxnetgo.utils.RxNetHelper.cache
import com.pingerx.rxnetgo.utils.RxNetHelper.error
import com.pingerx.rxnetgo.utils.RxNetHelper.scheduler
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONObject

/**
 * 网络请求工具类，提供请求相关的Rx操作
 * 线程切换：[scheduler]
 * 异常处理：[error]
 * 缓存：[cache]
 */
object RxNetHelper {

    /**
     * 订阅请求
     */
    fun <T> subscribe(request: Request<T>, subscriber: RxSubscriber<T>): Disposable {
        return rxAsync(request,subscriber)
                .subscribeWith(subscriber)
    }

    /**
     * 异步请求
     */
    fun <T> rxAsync(request: Request<T>, subscriber: RxSubscriber<T>?): Flowable<T> {
        return when (request.getMethod()) {
            RequestType.GET -> getAsync(request,subscriber)
            RequestType.POST -> postAsync(request,subscriber)
            RequestType.PUT -> putAsync(request, subscriber)
            RequestType.DELETE -> deleteAsync(request, subscriber)
        }
    }

    /**
     * 同步请求
     */
    @Throws(Exception::class)
    fun <T> rxSync(request: Request<T>): T? {
        return when (request.getMethod()) {
            RequestType.GET -> getSync(request)
            RequestType.POST -> postSync(request)
            RequestType.DELETE -> deleteSync(request)
            RequestType.PUT -> putSync(request)
        }
    }


    /**
     * Rx异步的Get请求
     */
    @SuppressLint("CheckResult")
    private fun <T> getAsync(request: Request<T>, subscriber: RxSubscriber<T>?): Flowable<T> {
        return if (request.apiService != null) {
            request.apiService
                    .getAsync(request.url, request.getHeaders().getHeaderParams(), request.getParams().getUrlParams())
                    .flatMap { response ->
                        var responseBody = response
                        var json = responseBody.string()
                        responseBody = ResponseBody.create(response.contentType(),json)
                        val data = request.getConverter().convertResponse(responseBody)
                        if (data != null) Flowable.just(data)
                        else {
                            if (subscriber!=null){
                                val jsonObject = JSONObject(json)
                                val msg  = jsonObject.get("msg")
                                val code = jsonObject.get("code")
                                Flowable
                                        .just(subscriber)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            it.onError(ApiException(msg = msg as String?,code = code as Int))
                                        }, {
//                                            LogUtils.e(it)
                                        })
                            }
                            Flowable.empty()
                        }
                    }
                    .cache(request.getCacheKey(), request.getConverter(), request.getCacheStrategy())
                    .compose()
        } else request.flowable ?: (request.flowable
                ?: Flowable.error(ApiException(code = NetErrorEngine.REQUEST_ERROR)))
    }

    /**
     * Rx异步的Post请求
     * Flowable.empty() 会直接进入onComplete回调
     */
    private fun <T> postAsync(request: Request<T>,subscriber: RxSubscriber<T>?): Flowable<T> {
        return if (request.apiService != null) {
            request.apiService
                    .postAsync(request.url, request.getHeaders().getHeaderParams(), request.getParams().getUrlParams(), request.generateRequestBody())
                    .flatMap { response ->
                        var responseBody = response
                        var json = responseBody.string()
                        responseBody = ResponseBody.create(response.contentType(),json)
                        val data = request.getConverter().convertResponse(responseBody)
                        if (data != null) Flowable.just(data)
                        else {
                            if (subscriber!=null){
                                val jsonObject = JSONObject(json)
                                val msg  = jsonObject.get("msg")
                                val code = jsonObject.get("code")
                                Flowable
                                        .just(subscriber)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            it.onError(ApiException(msg = msg as String?,code = code as Int))
                                        }, {
                                            //                                            LogUtils.e(it)
                                        })
                            }
                            Flowable.empty()
                        }
                    }
                    .cache(request.getCacheKey(), request.getConverter(), request.getCacheStrategy())
                    .compose()
        } else request.flowable ?: (request.flowable
                ?: Flowable.error(ApiException(code = NetErrorEngine.REQUEST_ERROR)))
    }


    /**
     * Rx异步PUT请求
     * Flowable.empty() 会直接进入onComplete回调
     */
    private fun <T> putAsync(request: Request<T>,subscriber: RxSubscriber<T>?): Flowable<T> {
        return if (request.apiService != null) {
            request.apiService
                    .putAsync(request.url, request.getHeaders().getHeaderParams(), request.getParams().getUrlParams(), request.generateRequestBody())
                    .flatMap { response ->
                        var responseBody = response
                        var json = responseBody.string()
                        responseBody = ResponseBody.create(response.contentType(),json)
                        val data = request.getConverter().convertResponse(responseBody)
                        if (data != null) Flowable.just(data)
                        else {
                            if (subscriber!=null){
                                val jsonObject = JSONObject(json)
                                val msg  = jsonObject.get("msg")
                                val code = jsonObject.get("code")
                                Flowable
                                        .just(subscriber)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            it.onError(ApiException(msg = msg as String?,code = code as Int))
                                        }, {
                                            //                                            LogUtils.e(it)
                                        })
                            }
                            Flowable.empty()
                        }
                    }
                    .cache(request.getCacheKey(), request.getConverter(), request.getCacheStrategy())
                    .compose()
        } else request.flowable ?: (request.flowable
                ?: Flowable.error(ApiException(code = NetErrorEngine.REQUEST_ERROR)))
    }

    /**
     * Rx异步的DELETE请求
     * Flowable.empty() 会直接进入onComplete回调
     */
    private fun <T> deleteAsync(request: Request<T>,subscriber: RxSubscriber<T>?): Flowable<T> {
        return if (request.apiService != null) {
            request.apiService
                    .deleteAsync(request.url, request.getHeaders().getHeaderParams(), request.generateRequestBody())
                    .flatMap { response ->
                        var responseBody = response
                        var json = responseBody.string()
                        responseBody = ResponseBody.create(response.contentType(),json)
                        val data = request.getConverter().convertResponse(responseBody)
                        if (data != null) Flowable.just(data)
                        else {
                            if (subscriber!=null){
                                val jsonObject = JSONObject(json)
                                val msg  = jsonObject.get("msg")
                                val code = jsonObject.get("code")
                                Flowable
                                        .just(subscriber)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            it.onError(ApiException(msg = msg as String?,code = code as Int))
                                        }, {
                                            //                                            LogUtils.e(it)
                                        })
                            }
                            Flowable.empty()
                        }
                    }
                    .cache(request.getCacheKey(), request.getConverter(), request.getCacheStrategy())
                    .compose()
        } else request.flowable ?: (request.flowable
                ?: Flowable.error(ApiException(code = NetErrorEngine.REQUEST_ERROR)))
    }

    /**
     * get同步请求，不需要使用Rx
     */
    @Throws(Exception::class)
    private fun <T> getSync(request: Request<T>): T? {
        return if (request.apiService != null) {
            val response = request.apiService
                    .getSync(request.url, request.getHeaders().getHeaderParams(), request.getParams().getUrlParams())
                    .execute()
                    .body()
            request.getConverter().convertResponse(response)
        } else null
    }


    /**
     * post同步请求，不需要使用Rx
     */
    @Throws(Exception::class)
    private fun <T> postSync(request: Request<T>): T? {
        return if (request.apiService != null) {
            val response = request.apiService
                    .postSync(request.url, request.getHeaders().getHeaderParams(), request.getParams().getUrlParams(), request.generateRequestBody())
                    .execute()
                    .body()
            request.getConverter().convertResponse(response)
        } else null
    }


    /**
     * delete同步请求，不需要使用Rx
     */
    @Throws(Exception::class)
    private fun <T> deleteSync(request: Request<T>): T? {
        return if (request.apiService != null) {
            val response = request.apiService
                    .deleteSync(request.url, request.getHeaders().getHeaderParams(), request.generateRequestBody())
                    .execute()
                    .body()
            request.getConverter().convertResponse(response)
        } else null
    }

    /**
     * put同步请求，不需要使用Rx
     */
    @Throws(Exception::class)
    private fun <T> putSync(request: Request<T>): T? {
        return if (request.apiService != null) {
            val response = request.apiService
                    .putSync(request.url, request.getHeaders().getHeaderParams(), request.getParams().getUrlParams(), request.generateRequestBody())
                    .execute()
                    .body()
            request.getConverter().convertResponse(response)
        } else null
    }

    /**
     * 生成组合线程切换，异常处理的Flowable
     */
    private fun <T> Flowable<T>.compose(): Flowable<T> {
        return scheduler().error()
    }
    /**
     * 异常处理
     */
    private fun <T> Flowable<T>.error(): Flowable<T> {
        return onErrorResumeNext(Function {
            var ex: Exception  = it as Exception
            if (it is ApiException){
                ex  = it as ApiException
            }
            Flowable.error<T> {
                val throwable = if (!TextUtils.isEmpty(ex.message)) ex
                else ex
                NetErrorEngine.handleException(throwable)
            }
        })
    }
    /**
     * 线程切换
     */
    private fun <T> Flowable<T>.scheduler(): Flowable<T> {
        return subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
    }
    /**
     * 缓存
     */
    private fun <T> Flowable<T>.cache(cacheKey: String, subscriber: IConverter<T>, strategy: ICacheStrategy): Flowable<T> {
        return rxCache(cacheKey, subscriber.getType(), strategy).flatMap {
            if (it.data != null) Flowable.just(it.data)
            else Flowable.empty()
        }
    }
}