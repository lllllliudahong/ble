package com.czw.newfit.ui.sport

import android.annotation.SuppressLint
import android.util.SparseArray
import android.view.View
import androidx.core.util.forEach
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.czw.newfit.R
import com.czw.newfit.adapter.AllSportListAdapter
import com.czw.newfit.api.ApiConstant
import com.czw.newfit.base.BoxActivity
import com.czw.newfit.bean.SportBean
import com.czw.newfit.bean.onTopicBeanList
import com.czw.newfit.databinding.ActivityAllSportBinding
import com.miekir.common.extension.lazy
import com.orhanobut.hawk.Hawk
import org.greenrobot.eventbus.EventBus

class AllSportActivity : BoxActivity<ActivityAllSportBinding>() {
    private val TAG = "AllSportActivity"
    private val mPresenter by lazy<AllSportActivity, AllSportPresenter>()

    override fun onBindingInflate() = ActivityAllSportBinding.inflate(layoutInflater)

    private var topicAdapter1: AllSportListAdapter? = null
    private var topicAdapter2: AllSportListAdapter? = null
    private val topicBeanList1 = ArrayList<SportBean>()
    private val topicBeanList2 = ArrayList<SportBean>()

    override fun onInit() {

        binding.llTitle.setTitle("运动管理")
        binding.llTitle.setTvRight("保存")
        binding.llTitle.setTvRightVisibility(View.VISIBLE)
        binding.llTitle.getTvRightView()?.setTextColor(getColor(R.color.color_fff66a31))
        binding.llTitle.getTvRightView()?.setOnClickListener {
            EventBus.getDefault().post(onTopicBeanList(topicBeanList1))
            finish()
        }

        topicBeanList1.clear()
        topicBeanList2.clear()

        val sportType = Hawk.get(ApiConstant.SPORT_TYPE, "3,11,13,12,7")
        val split = sportType.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sportAllList: SparseArray<SportBean> = SportType.getInstance().sportAllList
        //拿到选中的
        for (s in split) {
            val sportBean = sportAllList[s.toInt()]
            sportBean.isSelect = true
            topicBeanList1.add(sportBean)
        }
        //筛选未选中的
        sportAllList.forEach { key, value ->
            if (value.isSelect == false) {
                topicBeanList2.add(value)
            }
        }

        val list = ArrayList<SportBean>()//临时list
        list.addAll(topicBeanList1.filter { it.isShow == true })
        topicBeanList1.clear()
        topicBeanList1.addAll(list)
        list.clear()

        list.addAll(topicBeanList2.filter { it.isShow == true })
        topicBeanList2.clear()
        topicBeanList2.addAll(list)
        list.clear()
        initRecycleView()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecycleView() {
        binding.rvTopic1.layoutManager = GridLayoutManager(this, 4)
        topicAdapter1 = AllSportListAdapter(topicBeanList1)
        binding.rvTopic1.adapter = topicAdapter1

        binding.rvTopic2.layoutManager = GridLayoutManager(this, 4)
        topicAdapter2 = AllSportListAdapter(topicBeanList2)
        binding.rvTopic2.adapter = topicAdapter2

        topicAdapter1?.setOnItemClickListener { adapter, view, position ->
            if (topicBeanList1.size == 4) {
                ToastUtils.showLong("不能低于四项")
                return@setOnItemClickListener
            }
            topicBeanList2.add(topicBeanList1[position])
            topicBeanList1.removeAt(position)
            topicAdapter1?.notifyDataSetChanged()
            topicAdapter2?.notifyDataSetChanged()
        }
        topicAdapter2?.setOnItemClickListener { adapter, view, position ->
            topicBeanList1.add(topicBeanList2[position])
            topicBeanList2.removeAt(position)
            topicAdapter1?.notifyDataSetChanged()
            topicAdapter2?.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}