package com.czw.newfit.ui.health

import com.blankj.utilcode.util.ToastUtils
import com.czw.newfit.api.MainApi
import com.miekir.mvp.presenter.BasePresenter

class DeviceFragmentPresenter: BasePresenter<DeviceFragment>() {

    fun getAuthCode(phone: String, callback: (status: Int, path: String?) -> Unit) {
//        mView?.showPageLoadingDialog()
        ToastUtils.showLong("onLazyInit")
        MainApi.getAuthCode(phone, "change_pwd") {
            onSuccess {
                callback(200, it.code ?: "")
            }
            onFailed {
//                mView?.hidePageLoadingDialog()
                callback.invoke(0,"onFailed")
//                ToastUtils.showShort(it.getMsg())
            }
        }
    }

}