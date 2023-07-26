package com.czw.newfit.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.czw.newfit.R
import com.czw.newfit.bean.MeListItemBean

class MeListItemAdapter(treasureList: MutableList<MeListItemBean>): BaseQuickAdapter<MeListItemBean, BaseViewHolder>(
    R.layout.item_me_list_view, data = treasureList) {

    override fun convert(holder: BaseViewHolder, item: MeListItemBean) {
//
        val ivIcon = holder.getView<ImageView>(R.id.ivIcon)
        val tvTitle = holder.getView<TextView>(R.id.tvTitle)
        tvTitle.text = item.title
        ivIcon.setBackgroundResource(item.icon)
    }
}