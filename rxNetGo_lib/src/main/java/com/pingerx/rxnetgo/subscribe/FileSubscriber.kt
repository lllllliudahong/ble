package com.pingerx.rxnetgo.subscribe

import com.pingerx.rxnetgo.convert.FileConvert
import com.pingerx.rxnetgo.subscribe.base.BaseSubscriber
import okhttp3.ResponseBody
import java.io.File
import java.lang.reflect.Type

/**
 * 文件订阅者，生成本地文件
 *
 * @param fileDir 文件下载的目录
 * @param fileName 文件名
 * @param isDelete 是否删除已经存在的文件，默认删除
 *
 */
open class FileSubscriber(
        private val fileDir: String? = null,
        private val fileName: String? = null,
        private val isDelete: Boolean = true
) : BaseSubscriber<File>() {

    private var progressSubscriber: ((progress: Int, total: Long) -> Unit)? = null

    @Throws(Exception::class)
    override fun convertResponse(body: ResponseBody?): File {
        val convert = FileConvert(fileDir, fileName, isDelete)
        convert.setSubscribe(this)
        return convert.convertResponse(body)
    }

    override fun getType(): Type {
        return File::class.java
    }

    /**
     * 文件下载的进度
     * @param progress 百分比，0-100，下载的进度
     * @param downloaded 已下载多少字节
     * @param total 总共多少字节
     */
    fun onProgress(progressSubscriber: ((progress: Int, total: Long) -> Unit)?) {
        this.progressSubscriber = progressSubscriber
    }

    fun getProgressSubscriber(): ((progress: Int, total: Long) -> Unit)? {
        return progressSubscriber
    }
}