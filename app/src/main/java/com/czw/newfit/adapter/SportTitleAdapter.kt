package com.czw.newfit.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.czw.newfit.R
import com.czw.newfit.bean.SportBean
import com.czw.newfit.utils.AppUtils
import com.czw.newfit.utils.DisplayUtil
import com.czw.newfit.utils.LogUtils

class SportTitleAdapter(treasureList: MutableList<SportBean>): BaseQuickAdapter<SportBean, BaseViewHolder>(
    R.layout.item_sport_view, data = treasureList) {

    override fun convert(holder: BaseViewHolder, item: SportBean) {

        val tvText = holder.getView<TextView>(R.id.tv_topic_string)
        tvText.text = item.sportName
        if (item.isChecked == true){
            tvText.setTextColor(AppUtils.getColor(R.color.color_fff66a31))
        }else {
            tvText.setTextColor(AppUtils.getColor(R.color.color_ffa6a6a6))
        }
    }
}