package com.czw.newfit.ui.home

import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.czw.newfit.adapter.DragAdapter
import com.czw.newfit.base.BoxActivity
import com.czw.newfit.databinding.ActivityMoreTopicBinding
import com.miekir.common.extension.lazy

class MoreTopicActivity : BoxActivity<ActivityMoreTopicBinding>() {
    private val TAG = "MoreTopicActivity"
    private val mPresenter by lazy<MoreTopicActivity, MoreTopicPresenter>()

    override fun onBindingInflate() = ActivityMoreTopicBinding.inflate(layoutInflater)

    private var topicAdapter1: DragAdapter? = null
    private var topicAdapter2: DragAdapter? = null
    private val topicBeanListAll= ArrayList<String>()
    private val topicBeanList1= ArrayList<String>()
    private val topicBeanList2= ArrayList<String>()

    override fun onInit() {

        binding.llTitle.setTitle("编辑卡片")

        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("bbbbbbbbbbb")
        topicBeanListAll.add("ccccccccccc")
        topicBeanListAll.add("ddddddddddd")
        topicBeanListAll.add("fffffffffff")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")
        topicBeanListAll.add("aaaaaaaaaaa")

        topicBeanList1.clear()
        topicBeanList2.clear()
        if (topicBeanListAll.size > 4){
            topicBeanList1.addAll(topicBeanListAll.subList(0, 4))
            topicBeanList2.addAll(topicBeanListAll.subList(4, topicBeanListAll.size))
        }else{
            topicBeanList1.addAll(topicBeanListAll)
        }

        initTopRecycleView()
        initBtoRecycleView()

    }

    private fun initTopRecycleView() {
        binding.rvTopic1.layoutManager = GridLayoutManager(this, 2)
        topicAdapter1 = DragAdapter(this, topicBeanList1)
        binding.rvTopic1.adapter = topicAdapter1

        // 设置拖拽/滑动
        val dragCallBack = DragCallBack(topicAdapter1!!, topicBeanList1)
        val itemTouchHelper = ItemTouchHelper(dragCallBack)
        itemTouchHelper.attachToRecyclerView(binding.rvTopic1)

        topicAdapter1?.setOnItemClickListener(object : DragAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                Toast.makeText(this@MoreTopicActivity, dragCallBack.getData()[position], Toast.LENGTH_SHORT).show()
            }

            override fun onItemLongClick(holder: DragAdapter.ViewHolder) {
//                if (holder.adapterPosition != mAdapter.fixedPosition) {
                    itemTouchHelper.startDrag(holder)
//                }
            }
        })
        topicAdapter1?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                Log.i(TAG, "onItemRangeMoved")
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                Log.i(TAG, "onItemRangeRemoved")
            }

            override fun onChanged() {
                super.onChanged()
                Log.i(TAG, "onChanged")
            }
        })
    }

    private fun initBtoRecycleView() {
        binding.rvTopic2.layoutManager = GridLayoutManager(this, 2)
        topicAdapter2 = DragAdapter(this,topicBeanList2)
        binding.rvTopic2.adapter = topicAdapter2
        topicAdapter2?.setOnItemClickListener(object : DragAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
            }

            override fun onItemLongClick(holder: DragAdapter.ViewHolder) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}