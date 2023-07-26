package com.czw.newfit.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.KeyboardUtils
import com.miekir.mvp.view.binding.adapt.BindingFragment

/**
 * 基础Fragment
 */
abstract class BoxFragment<VB : ViewBinding> : BindingFragment<VB>() {
    companion object {
        const val SIZE_IN_DP_WIDTH = 375.0f
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getSizeInDp(): Float {
        return SIZE_IN_DP_WIDTH
    }

    override fun isBaseOnWidth(): Boolean {
        return true
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.run {
            KeyboardUtils.fixSoftInputLeaks(this)
        }
    }
}