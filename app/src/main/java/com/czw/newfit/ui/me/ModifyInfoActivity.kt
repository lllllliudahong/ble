package com.czw.newfit.ui.me

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.czw.newfit.R
import com.czw.newfit.application.MainApplication
import com.czw.newfit.base.BoxActivity
import com.czw.newfit.databinding.ActivityModifyInfoBinding
import com.czw.newfit.utils.BitmapUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.miekir.common.extension.lazy
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class ModifyInfoActivity : BoxActivity<ActivityModifyInfoBinding>() {
    private val TAG = "ModifyInfoActivity"
    private val mPresenter by lazy<ModifyInfoActivity, ModifyInfoPresenter>()

    override fun onBindingInflate() = ActivityModifyInfoBinding.inflate(layoutInflater)

    val TAKE_PHOTO = 1
    val PHOTO_ALBUM = 2
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var imageUri: Uri? = null
    private var outPutImage: File? = null
    private var imgFile: File? = null

    override fun onInit() {

        binding.llTitle.setTitleBgColor(getColor(R.color.color_background))
        binding.llTitle.setTitle("个人资料")

        binding.rlHeadIcon.setOnClickListener {
            showHeadIconDialog()
        }

        createOutImageFile()
    }

    private fun createOutImageFile() {
        outPutImage = File(externalCacheDir, "output_image.jpg")
        if (!outPutImage!!.exists()) {
            try {
                outPutImage!!.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun showHeadIconDialog() {
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog?.setContentView(R.layout.bottom_dialg_select_picture)
        bottomSheetDialog?.setCancelable(false)
        bottomSheetDialog?.setCanceledOnTouchOutside(true)
        bottomSheetDialog?.window?.findViewById<View>(com.blankj.utilcode.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)
        bottomSheetDialog?.findViewById<View>(R.id.tvCamera)?.setOnClickListener { v: View? ->
            bottomSheetDialog?.dismiss()
            openCamera()
        }
        bottomSheetDialog?.findViewById<View>(R.id.tvPhotoAlbum)?.setOnClickListener { v: View? ->
            bottomSheetDialog?.dismiss()
            openPhotoAlbum()
        }
        bottomSheetDialog?.findViewById<View>(R.id.tvCancel)?.setOnClickListener { v: View? ->
            if (bottomSheetDialog != null) {
                bottomSheetDialog?.dismiss()
            }
        }
        bottomSheetDialog?.show()
    }

    private fun openCamera() {
        try {
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                outPutImage?.let {
                    FileProvider.getUriForFile(
                        this@ModifyInfoActivity,
                        MainApplication.getApplication().packageName + ".fileProvider", it
                    )
                }
            } else {
                Uri.fromFile(outPutImage)
            }
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, TAKE_PHOTO)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openPhotoAlbum() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_ALBUM)
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val bitmap = BitmapFactory.decodeStream(
                        contentResolver.openInputStream(
                            imageUri!!
                        )
                    )
                    imgFile = rotateIfRequired(bitmap)?.let { BitmapUtil.bitmap2File(it) }
                    binding.ivHeadIcon.setImageURI(Uri.fromFile(imgFile))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == PHOTO_ALBUM) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val uri = data.data
                try {
                    val fileDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
                    val fileDescriptor1 = fileDescriptor!!.fileDescriptor
                    val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor1)
                    val inputStream = contentResolver.openInputStream(uri)
                    imgFile = inputStream?.let { rotateBitmap(bitmap, it)?.let { BitmapUtil.bitmap2File(it) } }
                    binding.ivHeadIcon.setImageURI(Uri.fromFile(imgFile))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun rotateIfRequired(bitmap: Bitmap): Bitmap? {
        try {
            val exifInterface = ExifInterface(outPutImage!!.path)
            val attributeInt: Int = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            var rotateBitmap: Bitmap? = null
            rotateBitmap = when (attributeInt) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
                else -> bitmap
            }
            return rotateBitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun rotateBitmap(bitmap: Bitmap, degress: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degress.toFloat())
        val rotateBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width,
            bitmap.height, matrix, true
        )
        bitmap.recycle()
        return rotateBitmap
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun rotateBitmap(bitmap: Bitmap, inputStream: InputStream): Bitmap? {
        try {
            val exifInterface = ExifInterface(inputStream)
            val attributeInt = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            var rotateBitmap: Bitmap? = null
            rotateBitmap = when (attributeInt) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
                else -> bitmap
            }
            return rotateBitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}