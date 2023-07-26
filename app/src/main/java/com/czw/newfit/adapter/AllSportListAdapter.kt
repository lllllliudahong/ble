package com.czw.newfit.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.czw.newfit.R
import com.czw.newfit.bean.MainTabItemBean
import com.czw.newfit.bean.SportBean
import com.czw.newfit.utils.AppUtils

/**
 * APP适配器
 */
class AllSportListAdapter(treasureList: MutableList<SportBean>): BaseQuickAdapter<SportBean, BaseViewHolder>(
    R.layout.layout_all_sport_item, data = treasureList) {

    override fun convert(holder: BaseViewHolder, item: SportBean) {

        val ivIcon = holder.getView<ImageView>(R.id.ivIcon)
        val tvText = holder.getView<TextView>(R.id.tvText)
        tvText.text = item.sportName
        ivIcon.setBackgroundResource(item.sportIcon)
        if (item.isSelect == false){
            tvText.setTextColor(AppUtils.getColor(R.color.color_FFFF8D1A))
        }else {
            tvText.setTextColor(AppUtils.getColor(R.color.color_FFFF8D1A))
        }

    }
}