package com.pingerx.rxnetgo.rxcache.stategy

import com.pingerx.rxnetgo.rxcache.core.RxCache
import com.pingerx.rxnetgo.rxcache.core.RxCacheHelper
import com.pingerx.rxnetgo.rxcache.data.CacheResult
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.lang.reflect.Type

/**
 * 只读取缓存的策略
 */
class OnlyCacheStrategy : ICacheStrategy {

    override fun <T> execute(rxCache: RxCache, key: String, source: Flowable<T>, type: Type): Publisher<CacheResult<T>> {
        return RxCacheHelper.loadCacheFlowable(rxCache, key, type, true)
    }
}