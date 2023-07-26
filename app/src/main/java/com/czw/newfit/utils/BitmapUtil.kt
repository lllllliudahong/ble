package com.czw.newfit.utils

import android.graphics.Bitmap
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class BitmapUtil {
    companion object {
        fun bitmap2File(bitmap: Bitmap): File {
            var externalFilesDir = AppUtils.getApplication().externalCacheDir
            var currentTimeMillis = System.currentTimeMillis()
            var tempFilePath = File(externalFilesDir, "$currentTimeMillis.jpg")

            var bos = BufferedOutputStream(FileOutputStream(tempFilePath))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
            return tempFilePath
        }
    }
}