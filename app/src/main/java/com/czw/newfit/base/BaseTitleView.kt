package com.czw.newfit.base

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.czw.newfit.R

/**
 * 统一title
 */
class BaseTitleView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    var contend: ConstraintLayout? = null
    var ivBack: ImageView? = null
    var title: TextView? = null
    var tvRight: TextView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.base_title_view, this)

        contend = findViewById(R.id.contend)
        ivBack = findViewById(R.id.ivBack)
        title = findViewById(R.id.tvTitle)
        tvRight = findViewById(R.id.tvRight)

        ivBack?.setOnClickListener {
            val activity = context as Activity
            activity.finish()
        }
    }

    /**
     * 设置title
     */
    fun setTitle(str: String) {
        title?.text = str
    }

    /**
     * 设置右边文字
     */
    fun setTvRight(str: String) {
        tvRight?.text = str
    }

    /**
     * 设置右边文字显示隐藏
     */
    fun setTvRightVisibility(visibility: Int) {
        tvRight?.visibility = visibility
    }

    /**
     * 获取右边文字View
     */
    fun getTvRightView(): TextView? {
        return tvRight
    }

    /**
     * 设置title背景颜色
     */
    fun setTitleBgColor(color: Int) {
        contend?.setBackgroundColor(color)
    }

}