package com.czw.newfit.ui.me

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.czw.newfit.R
import com.czw.newfit.adapter.MeListItemAdapter
import com.czw.newfit.base.BoxFragment
import com.czw.newfit.bean.MeListItemBean
import com.czw.newfit.databinding.FragmentMeBinding
import com.czw.newfit.utils.LaunchUtils
import com.miekir.common.extension.lazy

class MeFragment : BoxFragment<FragmentMeBinding>() {

    private val mPresenter by lazy<MeFragment, MeFragmentPresenter>()

    override fun onBindingInflate() = FragmentMeBinding.inflate(layoutInflater)

    private var mAdapter: MeListItemAdapter? = null
    private val mList= ArrayList<MeListItemBean>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onLazyInit() {
        mList.add(MeListItemBean(R.mipmap.ic_me_1, "账号与安全"))
        mList.add(MeListItemBean(R.mipmap.ic_me_2, "单位设置"))
        mList.add(MeListItemBean(R.mipmap.ic_me_3, "目标设置"))
        mList.add(MeListItemBean(R.mipmap.ic_me_4, "问题与建议"))
        mList.add(MeListItemBean(R.mipmap.ic_me_5, "问题与说明"))
        mList.add(MeListItemBean(R.mipmap.ic_me_6, "风格设置"))
        mList.add(MeListItemBean(R.mipmap.ic_me_7, "关于"))

        binding.rvRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = MeListItemAdapter(mList)
        binding.rvRecyclerView.adapter = mAdapter


        setOnClicks()

    }

    private fun setOnClicks(){
        mAdapter?.setOnItemClickListener { adapter, view, position ->  }
        //头像点击
        binding.robotCircleImageView.setOnClickListener {
            LaunchUtils.startModifyInfoActivity(this@MeFragment.activity)
        }
    }
}