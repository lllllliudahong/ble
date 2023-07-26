package com.czw.newfit.db

import org.greenrobot.greendao.query.Query
import org.greenrobot.greendao.query.QueryBuilder

/**
 * User: milan
 * Time: 2019/3/27 2:12
 * Des:
 */
interface IDatabase<M, K> {
    /**
     * 插入单条数据
     */
    fun insert(m: M): Boolean

    /**
     * 插入单条数据
     */
    fun insertForId(m: M): Long

    /**
     * 删除数据
     */
    fun delete(m: M): Boolean

    /**
     * 删除数据根据key
     */
    fun deleteByKey(key: K): Boolean

    /**
     * 删除多条数据
     */
    fun deleteList(list: List<M>): Boolean

    /**
     * 删除多条数据根据key
     */
    fun deleteByKeyInTx(vararg key: K): Boolean

    /**
     * 删除所有
     */
    fun deleteAll(): Boolean

    /**
     * 插入或替换
     */
    fun insertOrReplace(m: M): Boolean

    /**
     * 更新数据
     */
    fun update(m: M): Boolean

    /**
     * 更新多条数据
     */
    fun updateInTx(vararg m: M): Boolean

    /**
     * 更新多条数据
     */
    fun updateList(list: List<M>): Boolean

    /**
     * 查询单条数据
     */
    fun selectByPrimaryKey(key: K): M?

    /**
     * 加载所有数据
     */
    fun loadAll(): List<M>?

    /**
     * 分页加载
     *
     * @param page   设定当前页数
     * @param number 设定一页显示数量
     * @return
     */
    fun loadPages(page: Int, number: Int): List<M>?

    /**
     * 获取分页数
     *
     * @param number 设定一页显示数量
     * @return
     */
    fun getPages(number: Int): Long

    /**
     * 刷新数据
     */
    fun refresh(m: M): Boolean

    /**
     * 清理缓存
     */
    fun clearDaoSession()

    /**
     * 删除所有表和内容
     */
    fun dropDatabase(): Boolean

    /**
     * 事务
     */
    fun runInTx(runnable: Runnable)

    /**
     * 添加集合
     *
     */
    fun insertList(list: List<M>): Boolean

    /**
     * 添加集合
     */
    fun insertOrReplaceList(list: List<M>): Boolean

    /**
     * 自定义查询
     */
    val queryBuilder: QueryBuilder<M>?

    fun queryRaw(where: String, vararg selectionArg: String): List<M>?

    fun queryRawCreate(where: String, vararg selectionArg: String): Query<M>?

    fun queryRawCreateListArgs(where: String, selectionArg: Collection<String>): Query<M>?
}