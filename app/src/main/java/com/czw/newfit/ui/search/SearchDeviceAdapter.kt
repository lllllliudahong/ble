package com.czw.newfit.ui.search

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.czw.newfit.R
import com.czw.newfit.bean.ScanDeviceBean

/**
 * APP适配器
 */
class SearchDeviceAdapter(treasureList: MutableList<ScanDeviceBean>): BaseQuickAdapter<ScanDeviceBean, BaseViewHolder>(
    R.layout.adapter_search_device_item, data = treasureList) {

    @SuppressLint("MissingPermission")
    override fun convert(holder: BaseViewHolder, item: ScanDeviceBean) {
        // 图标
        val ivSignal = holder.getView<ImageView>(R.id.iv_signal)
        when (item.signal) {
            1 -> ivSignal.setImageResource(R.mipmap.bluetooth_signal_1)
            2 -> ivSignal.setImageResource(R.mipmap.bluetooth_signal_2)
            3 -> ivSignal.setImageResource(R.mipmap.bluetooth_signal_3)
            4 -> ivSignal.setImageResource(R.mipmap.bluetooth_signal_4)
            5 -> ivSignal.setImageResource(R.mipmap.bluetooth_signal_5)
        }

        val tvName = holder.getView<TextView>(R.id.tv_name)
        tvName.text = item.device?.name ?: ""

        val tvDeviceMac = holder.getView<TextView>(R.id.tv_device_mac)
        tvDeviceMac.text = item.device?.address ?: ""


    }
}