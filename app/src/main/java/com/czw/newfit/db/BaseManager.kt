package com.czw.newfit.db

import android.content.Context
import android.database.sqlite.SQLiteException
import com.blankj.utilcode.util.LogUtils
import com.czw.greendao.dao.DaoMaster
import com.czw.greendao.dao.DaoSession
import org.greenrobot.greendao.AbstractDao
import org.greenrobot.greendao.query.Query
import org.greenrobot.greendao.query.QueryBuilder

/**
 * User: milan
 * Time: 2019/3/27 2:12
 * Des:
 */
abstract class BaseManager<M, K> : IDatabase<M, K> {

    companion object {
        private const val DEFAULT_DATABASE_NAME = "manager.db"
        private lateinit var mHelper: DaoMaster.DevOpenHelper
        private lateinit var mDaoMaster: DaoMaster
        lateinit var daoSession: DaoSession

        /**
         * 初始化OpenHelper
         */
        fun initOpenHelper(context: Context) {
            initOpenHelper(context, DEFAULT_DATABASE_NAME)
        }

        /**
         * 初始化OpenHelper
         */
        fun initOpenHelper(context: Context, dataBaseName: String) {
            mHelper = getOpenHelper(context, dataBaseName)
            val helper = MyOpenHelper(context, dataBaseName, null)
            mDaoMaster = DaoMaster(helper.writableDatabase)
            openWritableDb()
        }

        /**
         * 在applicaiton中初始化DatabaseHelper
         */
        private fun getOpenHelper(context: Context, dataBaseName: String): DaoMaster.DevOpenHelper {
            closeDbConnections()
            return DaoMaster.DevOpenHelper(context, dataBaseName, null)
        }

        /**
         * 只关闭helper就好,看源码就知道helper关闭的时候会关闭数据库
         */
        private fun closeDbConnections() {
            if (this::mHelper.isInitialized)
                mHelper.close()
            if (this::daoSession.isInitialized)
                daoSession.clear()
        }


        private fun openReadableDb() {
            daoSession = mDaoMaster.newSession()
        }

        private fun openWritableDb() {
            daoSession = mDaoMaster.newSession()
        }
    }

    override fun insert(m: M): Boolean {
        try {
            openWritableDb()
            abstractDao().insert(m)
        } catch (e: SQLiteException) {
            LogUtils.e(e.message,e.toString())
            return false
        }
        return true
    }

    override fun insertForId(m: M): Long {
        return try {
            openWritableDb()
            abstractDao().insert(m)
        } catch (e: SQLiteException) {
            -1
        }
    }

    override fun delete(m: M): Boolean {
        try {
            openWritableDb()
            abstractDao().delete(m)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun deleteByKey(key: K): Boolean {
        try {
            if (key.toString().isEmpty()) return false
            openWritableDb()
            abstractDao().deleteByKey(key)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun deleteList(list: List<M>): Boolean {
        try {
            if (list.isEmpty()) return false
            openWritableDb()
            abstractDao().deleteInTx(list)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun deleteByKeyInTx(vararg key: K): Boolean {
        try {
            openWritableDb()
            abstractDao().deleteByKeyInTx(*key)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun deleteAll(): Boolean {
        try {
            openWritableDb()
            abstractDao().deleteAll()
        } catch (e: SQLiteException) {
            LogUtils.e(e.message)
            return false
        }
        return true
    }

    override fun insertOrReplace(m: M): Boolean {
        try {
            openWritableDb()
            abstractDao().insertOrReplace(m)
        } catch (e: SQLiteException) {
//            LogUtils.e(e.message,e.toString())
            return false
        }
        return true
    }

    override fun update(m: M): Boolean {
        try {
            openWritableDb()
            abstractDao().update(m)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun updateInTx(vararg m: M): Boolean {
        try {
            openWritableDb()
            abstractDao().updateInTx(*m)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun updateList(list: List<M>): Boolean {
        try {
            if (list.isEmpty()) return false
            openWritableDb()
            abstractDao().updateInTx(list)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun selectByPrimaryKey(key: K): M? {
        return try {
            openReadableDb()
            abstractDao().load(key)
        } catch (e: SQLiteException) {
            null
        }
    }

    override fun loadAll(): List<M>? {
        openReadableDb()
        return abstractDao().loadAll()
    }

    override fun loadPages(page: Int, number: Int): List<M>? {
        openReadableDb()
        return abstractDao().queryBuilder()
                .offset(page * number).limit(number).list()
    }

    override fun getPages(number: Int): Long {
        val count = abstractDao().queryBuilder().count()
        val page = count / number
        return if (page > 0 && count % number == 0L) {
            page - 1
        } else page
    }

    override fun refresh(m: M): Boolean {
        try {
            openWritableDb()
            abstractDao().refresh(m)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun clearDaoSession() {
        daoSession.clear()
    }

    override fun dropDatabase(): Boolean {
        try {
            openWritableDb()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun runInTx(runnable: Runnable) {
        try {
            openWritableDb()
            daoSession.runInTx(runnable)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    override fun insertList(list: List<M>): Boolean {
        try {
            if (list.isEmpty()) return false
            openWritableDb()
            abstractDao().insertInTx(list)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    override fun insertOrReplaceList(list: List<M>): Boolean {
        try {
            if (list.isEmpty()) return false
            openWritableDb()
            abstractDao().insertOrReplaceInTx(list)
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }




    override val queryBuilder: QueryBuilder<M>?
        get() {
            openReadableDb()
            return abstractDao().queryBuilder()
        }

    override fun queryRaw(where: String, vararg selectionArg: String): List<M>? {
        openReadableDb()
        return abstractDao().queryRaw(where, *selectionArg)
    }

    override fun queryRawCreate(where: String, vararg selectionArg: String): Query<M>? {
        openReadableDb()
        return abstractDao().queryRawCreate(where, *selectionArg)
    }

    override fun queryRawCreateListArgs(where: String, selectionArg: Collection<String>): Query<M>? {
        openReadableDb()
        return abstractDao().queryRawCreateListArgs(where, selectionArg)
    }

    /**
     * 获取Dao
     *
     * @return
     */
    abstract fun abstractDao(): AbstractDao<M, K>

}