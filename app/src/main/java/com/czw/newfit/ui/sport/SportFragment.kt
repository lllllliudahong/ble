package com.czw.newfit.ui.sport

import android.annotation.SuppressLint
import android.util.SparseArray
import androidx.recyclerview.widget.LinearLayoutManager
import com.czw.newfit.adapter.SportTitleAdapter
import com.czw.newfit.api.ApiConstant
import com.czw.newfit.base.BoxFragment
import com.czw.newfit.bean.SportBean
import com.czw.newfit.bean.onTopicBeanList
import com.czw.newfit.databinding.FragmentSportBinding
import com.czw.newfit.utils.LaunchUtils
import com.czw.newfit.utils.PermissionUtil
import com.miekir.common.extension.lazy
import com.orhanobut.hawk.Hawk
import com.permissionx.guolindev.PermissionX
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SportFragment : BoxFragment<FragmentSportBinding>() {

    private val mPresenter by lazy<SportFragment, SportFragmentPresenter>()

    override fun onBindingInflate() = FragmentSportBinding.inflate(layoutInflater)

    private val sportTitleList: ArrayList<SportBean> = ArrayList()
    private var sportTitleAdapter: SportTitleAdapter? = null

    private var selectType: Byte = 0//选中的运动

    @SuppressLint("NotifyDataSetChanged")
    override fun onLazyInit() {
        EventBus.getDefault().register(this)

        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.rvSportList.layoutManager = linearLayoutManager
        sportTitleAdapter = SportTitleAdapter(sportTitleList)
        binding.rvSportList.adapter = sportTitleAdapter
        initData()

        sportTitleAdapter?.setOnItemClickListener { adapter, view, position ->
            sportTitleList.forEach {
                it.isChecked = false
            }
            sportTitleList[position].isChecked = true
            selectType = sportTitleList[position].type
            setType()
            adapter.notifyDataSetChanged()
        }

        binding.rlAddSportType.setOnClickListener {
            LaunchUtils.startActivity(activity, AllSportActivity::class.java)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initData() {
        val sportType = Hawk.get(ApiConstant.SPORT_TYPE, "3,11,13,12,7")
        val split = sportType.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (s in split) {
            val sportTypeList: SparseArray<SportBean> = SportType.getInstance().sportAllList
            val sportBean = sportTypeList[s.toInt()]
            sportTitleList.add(sportBean)
        }
//        SportType.getInstance().sportAllList.forEach { key, value ->
////            LogUtils.d("value.type = ${String.format("%02x", value.type)}")
//            if (sportType.contains(String.format("%02x", value.type))) {
//                sportTitleList.add(value)
//            }
//        }
        val list = ArrayList<SportBean>()//临时list
        list.addAll(sportTitleList.filter { it.isShow == true })
        sportTitleList.clear()
        sportTitleList.addAll(list)
        list.clear()

        if (sportTitleList.size > 0) {
            sportTitleList.first().isChecked = true
            selectType = sportTitleList.first().type
            setType()
        }
        sportTitleList.filter { it.isShow == true }
        sportTitleAdapter?.notifyDataSetChanged()
    }

    private fun setType(){
        binding.tvType.text = selectType.toString()
    }

    override fun onResume() {
        //需要定位权限
        checkPermission()
        super.onResume()
    }


    private fun checkPermission() {
        PermissionX.init(this@SportFragment)
            .permissions(permissions = PermissionUtil.getInstance().REQUIRED_PERMISSION_LIST)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {

                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onTopicBeanList(bean: onTopicBeanList) {
        sportTitleList.clear()
        sportTitleList.addAll(bean.topicBeanList)
        sportTitleAdapter?.notifyDataSetChanged()

        var sport_type = ""
        var isGetSelectType = false//用来判断新选择的类型，有没有上次的类型
        sportTitleList.forEach {
            sport_type = "$sport_type${it.type},"
            if (String.format("%02x", it.type) == String.format("%02x", selectType)){
                it.isChecked = true
                isGetSelectType = true
            }else{
                it.isChecked = false
            }
        }
        //没有上次选中的类型，默认第一个
        if (!isGetSelectType){
            sportTitleList.first().isChecked = true
            selectType = sportTitleList.first().type
            setType()
        }
        if (sport_type.isNotEmpty()) {
            Hawk.put(ApiConstant.SPORT_TYPE, sport_type.subSequence(0, sport_type.length - 1))
        }

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}