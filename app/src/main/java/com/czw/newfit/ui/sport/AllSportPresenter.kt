package com.czw.newfit.ui.sport

import com.czw.newfit.api.MainApi
import com.miekir.mvp.presenter.BasePresenter

class AllSportPresenter: BasePresenter<AllSportActivity>() {

    fun getAuthCode(phone: String, callback: (status: Int, path: String?) -> Unit) {
//        mView?.showPageLoadingDialog()
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