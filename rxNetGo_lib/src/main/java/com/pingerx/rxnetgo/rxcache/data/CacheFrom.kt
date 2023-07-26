package com.pingerx.rxnetgo.rxcache.data

/**
 * 缓存数据来源：远程，磁盘和内存
 */
enum class CacheFrom {
    Remote,
    Disk,
    Memory;

    companion object {
        fun isFromCache(from: CacheFrom): Boolean {
            return from == Disk || from == Memory
        }
    }
}
