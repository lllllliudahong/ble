package com.czw.newfit.api

import android.text.TextUtils
import com.czw.newfit.utils.JsonUtils
import com.czw.newfit.utils.LogUtils
import com.pingerx.rxnetgo.convert.base.IConverter
import com.pingerx.rxnetgo.exception.ApiException
import com.pingerx.rxnetgo.exception.NetErrorEngine
import okhttp3.ResponseBody
import org.json.JSONObject
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 * 数据转换
 */
class ApiConvert<T>(private var type: Type? = null,
                    private val clazz: Class<T>? = null) : IConverter<T> {

    companion object{
        fun isCodeSuccess(code : Int) : Boolean = code == ErrorCode.SUCCESS || code == ErrorCode.SUCCESS_CODE

        fun <V> parse(type : Type, json : String, listener : ParseListener<V>){

            if (TextUtils.isEmpty(json)){
                if(listener != null)
                    listener.onFailure(NetErrorEngine.DATA_ERROR, "", null, json)
                return
            }

            val jsonObject = JSONObject(json)
            val code = jsonObject.optInt("code", -1)
            var message = jsonObject.optString("msg", "unknown message.")

            if (code == 4999) {
                LogUtils.e("api convert state code = $code")
//                ToastUtils.showCustomToast("登录权限校验失败")
//                DaoRepository.deleteUser(ActivityUtils.getTopActivity())

                if(listener != null)
                    listener.onFailure(401, message, null, json)

                return
            }

            val bean = JsonUtils.fromJson(json, BaseApiCommonBean::class.java)

            val dataStr = jsonObject.optString("data", "")
            if (dataStr != null && dataStr != "null" && dataStr != "{}" && dataStr.trim() != "" && dataStr != "[]") {
                if (bean.code == ErrorCode.SUCCESS || bean.code == ErrorCode.SUCCESS_CODE || bean.code == 100200 || bean.msg == "ok") {
                    val data = bean.data
                    // 后端返回的code是成功的，但是data会空的 } else null
                    if(listener != null){
                        if (data != null) {
                            listener.onSuccess(code, message, JsonUtils.fromJson(data, type), json)
                        } else {
                            listener.onFailure(code, message, null, json)
                        }
                    }

                } else {
                    // 根据服务端的code来分发消息
                    if(listener != null){
                        listener.onFailure(code, message, null, json)
                    }
                }
            } else {
                if(listener != null){
                    listener.onFailure(code, message, null, json)
                }
            }

        }
    }

    override fun convertResponse(body: ResponseBody?): T? {
        val json = body?.string() ?: ""
        body?.close()
        if (TextUtils.isEmpty(json)) throw ApiException(code = NetErrorEngine.DATA_ERROR)
        val jsonObject = JSONObject(json)
        val dataStr = jsonObject.optString("data", "")
        val code = jsonObject.optInt("code", -1)
        var message = jsonObject.optString("msg", "unknown message.")
        if (code == 4999) {
            LogUtils.e("api convert state code = $code -- message = $message")
//            DaoRepository.deleteUser(ActivityUtils.getTopActivity())
//            ToastUtils.showCustomToast("登录权限校验失败")
            return null
        }
        val bean = JsonUtils.fromJson(json, BaseApiCommonBean::class.java)
        return convert(dataStr, message, code, bean)
    }

    private fun convert(dataStr: String?, message: String, code: Int, bean: BaseApiBean): T? {
        if (dataStr != null && dataStr != "null" && dataStr != "{}" && dataStr.trim() != "" && dataStr != "[]") {
            if (bean.code == ErrorCode.SUCCESS || bean.code == ErrorCode.SUCCESS_CODE || bean.msg == "ok") {
                val data = bean.data
                // 后端返回的code是成功的，但是data会空的 } else null
                return if (data != null) {
                    JsonUtils.fromJson(data, getType())
                } else {
                    throw ApiException(msg = message, code = code)
                }
            } else {
                // 根据服务端的code来分发消息
                throw ApiException(msg = bean.msg, code = code)
            }
        } else {
            throw ApiException(msg = message, code = code)
        }
    }

    override fun getType(): Type {
        val type = type
        return type ?: if (clazz == null) {
            val genType = javaClass.genericInterfaces[0]
            (genType as ParameterizedType).actualTypeArguments[0]
        } else clazz
    }

    interface ParseListener<V>{
        fun onSuccess(code : Int, msg : String, data : V?, jsonStr : String)
        fun onFailure(code : Int, msg : String, data : V?, jsonStr : String)
    }
}