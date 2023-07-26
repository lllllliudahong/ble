package com.czw.newfit.bean

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MainTabItemBean(
        var selectRes: Int,
        var nuSelectRes: Int,
        var title: String,
        var isSelect: Boolean = false
): Parcelable

@Parcelize
data class AuthCodeBean(
        var code: String?
): Parcelable

@Parcelize
data class ScanDeviceBean(
        var device: BluetoothDevice?,
        var scanRecords: ByteArray,
        var signal: Int,
        var deviceMode: Int//0;//默认设备为普通设备 1为杰理设备
): Parcelable

@Parcelize
data class MeListItemBean(
        var icon: Int,
        var title: String
): Parcelable

@Parcelize
data class SportBean(
        var sportName: String,
        var sportIcon: Int,
        var type: Byte,
        var isSelect: Boolean?,//true显示在首页tab
        var isChecked: Boolean?,//true显示选中状态
        var isShow: Boolean?,//true显示在选择列表
): Parcelable
