package com.pingerx.rxnetgo.utils


import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.net.URLDecoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.ArrayList
import java.util.Random

object EncryptUtil {
    private val clientId = ""


    private fun getMD5(str: String): String? {
        val digest = MessageDigest.getInstance("MD5")
        val result = digest.digest(str.toByteArray())
        //没转16进制之前是16位
        println("result${result.size}")
        //转成16进制后是32字节
        return toHex(result)

    }
    fun toHex(byteArray: ByteArray): String {
        val result = with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    this.append("0").append(hexStr)
                } else {
                    this.append(hexStr)
                }
            }
            this.toString()
        }
        //转成16进制后是32字节
        return result
    }

    fun getEncrypt(t: String): String? {
        val secretKey = "72771df8d9b675411cf41d9fff26889f$t"
        return getMD5(secretKey)
    }


    fun getString(bytes: ByteArray?): String? {

        try {
            return if (bytes != null) {
                String(bytes, Charset.forName("UTF-8"))
            } else ""
        } catch (e: UnsupportedEncodingException) {
        }

        return null
    }

    private fun createRandomCharData(length: Int): String {
        val sb = StringBuilder()
        val rand = Random()//随机用以下三个随机生成器
        val randdata = Random()
        var data = 0
        for (i in 0 until length) {
            val index = rand.nextInt(3)
            //目的是随机选择生成数字，大小写字母
            when (index) {
                0 -> {
                    data = randdata.nextInt(10)//仅仅会生成0~9
                    sb.append(data)
                }
                1 -> {
                    data = randdata.nextInt(26) + 65//保证只会产生65~90之间的整数
                    sb.append(data.toChar())
                }
                2 -> {
                    data = randdata.nextInt(26) + 97//保证只会产生97~122之间的整数
                    sb.append(data.toChar())
                }
            }
        }
        return sb.toString()
    }

    fun toJsonString(params: Map<String, Any>): String {
        try {
            val values = ArrayList<String?>()
            val names = ArrayList(params.keys)
            names.sort()
            for (i in names.indices) {
                values.add(params[names[i]].toString())
            }
            val sb = StringBuffer()
            for (i in names.indices) {
                sb.append(names[i])
                sb.append(URLDecoder.decode(values[i]))
            }
            return sb.toString()
        }catch(e:Exception){
            e.printStackTrace()
        }
        return ""
    }
}