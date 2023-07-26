package com.pingerx.rxnetgo.rxcache.stategy

import com.pingerx.rxnetgo.rxcache.core.RxCache
import com.pingerx.rxnetgo.rxcache.data.CacheResult
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.lang.reflect.Type

/**
 * 缓存策略基类，提取实现缓存的方法
 */
interface ICacheStrategy {

    fun <T> execute(rxCache: RxCache, key: String, source: Flowable<T>, type: Type): Publisher<CacheResult<T>>
}