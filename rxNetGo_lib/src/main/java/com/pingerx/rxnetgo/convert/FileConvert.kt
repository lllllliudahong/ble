package com.pingerx.rxnetgo.convert

import com.pingerx.rxnetgo.RxNetGo
import com.pingerx.rxnetgo.convert.base.IConverter
import com.pingerx.rxnetgo.subscribe.FileSubscriber
import com.pingerx.rxnetgo.utils.HttpUtils
import com.pingerx.rxnetgo.utils.NetLogger
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type

/**
 * 文件转换器，将相应流中的数据保存到本地文件
 * 不支持断点续传，如果需要多文件下载并且断点续传，使用rxdownload类库
 */
class FileConvert(
        private val fileDir: String? = null,
        private val fileName: String? = null,
        private val isDelete: Boolean = true

) : IConverter<File> {

    private var mSubscriber: FileSubscriber? = null

    override fun convertResponse(body: ResponseBody?): File {

        val file = HttpUtils.createDownloadFile(fileDir, fileName, isDelete)
        val byteStream = body?.byteStream()
        val contentLength = body?.contentLength() ?: 0

        if (byteStream == null) {
            return file
        }

        val buf = ByteArray(2048)
        var outStream: FileOutputStream? = null
        try {
            outStream = FileOutputStream(file)
            var downloaded = 0L
            var len = byteStream.read(buf)
            while (len != -1) {
                downloaded += len
                outStream.write(buf, 0, len)
                len = byteStream.read(buf)

                val progress = if (contentLength.toInt() == -1 || contentLength == 0L) {
                    100
                } else {
                    (downloaded * 100 / contentLength).toInt()
                }
                RxNetGo.getInstance().getHandler().post {
                    mSubscriber?.getProgressSubscriber()?.invoke(progress, contentLength)
                }
                NetLogger.e("====$progress, $contentLength")
            }
            outStream.flush()
        } finally {
            outStream?.close()
            byteStream.close()
            body.close()
        }

        return file
    }

    fun setSubscribe(fileSubscriber: FileSubscriber) {
        this.mSubscriber = fileSubscriber
    }

    override fun getType(): Type {
        return File::class.java
    }
}