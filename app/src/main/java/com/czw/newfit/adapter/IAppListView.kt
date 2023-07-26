package com.czw.newfit.adapter

import com.miekir.mvp.view.base.IView

interface IAppListView: IView {
    fun onAppList(it: List<String>)
}