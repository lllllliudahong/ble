package com.czw.newfit.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.czw.newfit.R
import com.czw.newfit.utils.AppUtils

class FunctionItemViewAdapter(treasureList: MutableList<String>): BaseQuickAdapter<String, BaseViewHolder>(
    R.layout.item_function_view, data = treasureList) {

    override fun convert(holder: BaseViewHolder, item: String) {
//
//        val ivIcon = holder.getView<ImageView>(R.id.ivIcon)
        val tvText = holder.getView<TextView>(R.id.tv_topic_string)
        tvText.text = item
        if (state){
            tvText.setTextColor(AppUtils.getColor(R.color.black))
        }else{
            tvText.setTextColor(AppUtils.getColor(R.color.grey_80))
        }
    }

    private var state = false
    fun setState(b: Boolean){
        state = b
    }
}