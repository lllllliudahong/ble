package com.czw.newfit.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.czw.newfit.R
import com.czw.newfit.bean.MainTabItemBean
import com.czw.newfit.utils.AppUtils

/**
 * APP适配器
 */
class TabListAdapter(treasureList: MutableList<MainTabItemBean>): BaseQuickAdapter<MainTabItemBean, BaseViewHolder>(
    R.layout.layout_main_btm_item, data = treasureList) {

    override fun convert(holder: BaseViewHolder, item: MainTabItemBean) {

        val ivIcon = holder.getView<ImageView>(R.id.ivIcon)
        val tvText = holder.getView<TextView>(R.id.tvText)
        tvText.text = item.title
        if (item.isSelect){
            ivIcon.setBackgroundResource(item.selectRes)
            tvText.setTextColor(AppUtils.getColor(R.color.color_FFFF8D1A))
        }else {
            ivIcon.setBackgroundResource(item.nuSelectRes)
            tvText.setTextColor(AppUtils.getColor(R.color.color_FFFF8D1A))
        }

    }
}