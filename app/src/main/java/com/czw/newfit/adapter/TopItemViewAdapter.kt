package com.czw.newfit.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.czw.newfit.R

class TopItemViewAdapter(treasureList: MutableList<String>): BaseQuickAdapter<String, BaseViewHolder>(
    R.layout.item_topic_view, data = treasureList) {

    override fun convert(holder: BaseViewHolder, item: String) {
//
//        val ivIcon = holder.getView<ImageView>(R.id.ivIcon)
//        val tvText = holder.getView<TextView>(R.id.tvText)
//        tvText.text = item.title
//        if (item.isSelect){
//            ivIcon.setBackgroundResource(item.selectRes)
//            tvText.setTextColor(AppUtils.getColor(R.color.deep))
//        }else {
//            ivIcon.setBackgroundResource(item.nuSelectRes)
//            tvText.setTextColor(AppUtils.getColor(R.color.color_999999))
//        }

    }
}