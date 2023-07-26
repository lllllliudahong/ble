package com.czw.newfit.utils

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

object LayoutManagerUtil {

    @JvmStatic
    fun getVerticalLinearLayoutManager(context: Context?): WrapContentLinearLayoutManager {
        return WrapContentLinearLayoutManager(context)
    }

    @JvmStatic
    fun getHorizontalLinearLayoutManager(context: Context?): WrapContentLinearLayoutManager {
        return WrapContentLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    @JvmStatic
    fun getGridLayoutManager(context: Context?, spanCount: Int): GridLayoutManager {
        return GridLayoutManager(context, spanCount)
    }
}
