package com.czw.newfit.db

import com.czw.newfit.api.ApiConstant
import com.czw.newfit.bean.FreeFitStepsBean
import com.czw.newfit.bean.FreeFitSupportFunction
import com.czw.newfit.utils.DateUtils
import com.czw.greendao.dao.FreeFitStepsBeanDao
import org.greenrobot.greendao.AbstractDao

/**
 * <pre>
 * @user : milanxiaotiejiang
 * @email : 765151629@qq.com
 * @version : 1.0
 * @date : 2020/8/17
 * @description : 数据库管理类
 * </pre>
 */
class FreeFitStepsBeanManager : BaseManager<FreeFitStepsBean, Long>() {
    override fun abstractDao(): AbstractDao<FreeFitStepsBean, Long> = daoSession.freeFitStepsBeanDao

    fun queryDayStepData(time: Long): FreeFitStepsBean? {
        return abstractDao().queryBuilder()
            .where(
                FreeFitStepsBeanDao.Properties.Time.eq(DateUtils.formatTime(time, ApiConstant.TIME_FORMAT)),
                FreeFitStepsBeanDao.Properties.Type.eq(FreeFitStepsBean.TYPE_TOTAL)
            )
            .build()
            .unique()
    }
}

class FreeFitSupportFunctionManager : BaseManager<FreeFitSupportFunction, Long>() {
    override fun abstractDao(): AbstractDao<FreeFitSupportFunction, Long> = daoSession.freeFitSupportFunctionDao
}

object DBManager {
    val mFreeFitStepsBeanManager = FreeFitStepsBeanManager()
    val mFreeFitSupportFunctionManager = FreeFitSupportFunctionManager()
}
