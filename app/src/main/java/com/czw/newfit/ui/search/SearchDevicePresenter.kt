package com.czw.newfit.ui.search

import com.czw.newfit.adapter.IAppListView
import com.czw.newfit.api.MainApi
import com.miekir.mvp.presenter.BasePresenter

class SearchDevicePresenter: BasePresenter<IAppListView>() {
    val appList = ArrayList<String>()


    fun getList(phone: String) {
//        mView?.showPageLoadingDialog()
        MainApi.getAuthCode(phone, "change_pwd") {
            onSuccess {
            }
            onFailed {
                appList.clear()
                appList.add("1111111111111")
                appList.add("2222222222222")
                appList.add("3333333333333")
                appList.add("4444444444444")
                appList.add("5555555555555")
                appList.add("6666666666666")
                appList.add("7777777777777")
                appList.add("8888888888888")
                view?.onAppList(appList)
//                mView?.hidePageLoadingDialog()
//                ToastUtils.showShort(it.getMsg())
            }
        }
    }

}