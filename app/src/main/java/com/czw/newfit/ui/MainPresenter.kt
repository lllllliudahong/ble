package com.czw.newfit.ui

import com.czw.newfit.api.MainApi
import com.miekir.mvp.presenter.BasePresenter

/**
 * @date 2022-1-22 21:51
 * @author 詹子聪
 */
class MainPresenter: BasePresenter<MainActivity>() {

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