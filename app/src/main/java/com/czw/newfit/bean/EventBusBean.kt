package com.czw.newfit.bean

import android.bluetooth.BluetoothDevice


data class onDeviceStateChange(
        var status: Int,
        var device: BluetoothDevice?
)

data class RealTimeSteps(
        var freeFitStepsBean: FreeFitStepsBean,
)
data class onBatteryNum(
        var battery: Int
)
data class onActivityResultBean(
        var requestCode: Int
)
data class onTopicBeanList(
        var topicBeanList: ArrayList<SportBean>
)

