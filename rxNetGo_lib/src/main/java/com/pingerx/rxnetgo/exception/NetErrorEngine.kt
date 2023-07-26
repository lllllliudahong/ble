package com.pingerx.rxnetgo.exception


import android.util.Log
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException

/**
 * 封装请求错误的提示信息和错误码，统一各种异常，将结果封装成[ApiException]，然后在onError方法中返回
 */
object NetErrorEngine {

    // 服务端的异常，对应HTTP的状态码
    const val UNAUTHORIZED = 401
    private const val FORBIDDEN = 403
    private const val NOT_FOUND = 404
    private const val REQUEST_TIMEOUT = 408
    private const val INTERNAL_SERVER_ERROR = 500
    private const val BAD_GATEWAY = 502
    private const val SERVICE_UNAVAILABLE = 503
    private const val GATEWAY_TIMEOUT = 504

    // 本地请求数据和解析等发生的异常码
    const val UNKNOWN_CODE = 1000
    const val SERVER_ERROR_CODE = 1003
    const val PARSE_ERROR_CODE = 1001
    private const val NETWORD_ERROR_CODE = 1002
    private const val NETWORK_UNCONNECTED = 1004
    const val DATA_ERROR = 1005
    const val REQUEST_ERROR = 1006
    private const val UNKNOW_HOST_ERROR_CODE = 1007
    private const val NETWORD_TIMEOUT_CODE = 1008

    private const val UNKNOW_HOST_ERROR_MSG = "网络异常，请稍后再试"
    private const val PARSE_ERROR_MSG = "网络异常，请稍后再试"
    private const val REQUEST_ERROR_MSG = "数据请求异常"
    private const val NETWORD_ERROR_MSG = "网络连接异常"
    private const val SERVER_ERROR_MSG = "服务器异常"
    private const val DATA_ERROR_MSG = "服务器异常"
    const val UNKNOW_MSG = "未知错误"
    private const val NETWORK_UNCONNECTED_MSG = "网络连接异常，请检查网络"
    private const val NETWORD_TIMEOUT = "网络连接超时，请稍后再试"


    /**
     * 分发网络请求的异常
     */
    fun handleException(e: Throwable): ApiException {
        e.printStackTrace()
        if (e is HttpException) {
            val exception = ApiException(e, SERVER_ERROR_CODE)
            when (e.code()) {
                UNAUTHORIZED,FORBIDDEN, NOT_FOUND, REQUEST_TIMEOUT, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE -> exception.setMsg(SERVER_ERROR_MSG)
                else -> {
                    exception.setMsg(SERVER_ERROR_MSG)
                }
            }
            return exception
        } else return if (e is UnknownHostException) {
            ApiException(e, UNKNOW_HOST_ERROR_CODE, UNKNOW_HOST_ERROR_MSG)
        } else if (e is JsonParseException
                || e is JSONException
                || e is JsonSyntaxException
                || e is ParseException) {
            // 解析错误
            ApiException(e, PARSE_ERROR_CODE, PARSE_ERROR_MSG)
        } else if (e is ConnectException) {
            // 网络错误
            ApiException(e, NETWORD_ERROR_CODE, NETWORD_ERROR_MSG)
        } else if(e is SocketTimeoutException){
            // 网络超时
            ApiException(e, NETWORD_TIMEOUT_CODE, NETWORD_TIMEOUT)
        } else if (e is ApiException) {
            // 如果是自己就直接返回
            return parseApiException(e)
        } else {
            // 未知错误
            ApiException(e, UNKNOWN_CODE, UNKNOW_MSG)
        }
    }

    private fun parseApiException(e: ApiException): ApiException {
        when (e.getCode()) {
            UNKNOWN_CODE -> e.setMsg(UNKNOW_MSG)
            PARSE_ERROR_CODE -> e.setMsg(PARSE_ERROR_MSG)
            NETWORD_ERROR_CODE -> e.setMsg(NETWORD_ERROR_MSG)
            SERVER_ERROR_CODE -> e.setMsg(SERVER_ERROR_MSG)
            UNKNOW_HOST_ERROR_CODE -> e.setMsg(UNKNOW_HOST_ERROR_MSG)
            NETWORK_UNCONNECTED -> e.setMsg(NETWORK_UNCONNECTED_MSG)
            DATA_ERROR -> e.setMsg(DATA_ERROR_MSG)
        }
        return e
    }
}
