package com.czw.newfit.api

import com.google.gson.JsonElement

/**
 * 最上层的Api数据结构
 * data字段有可能返回null
 */
open class BaseApiBean(@Transient open val code: Int, @Transient open val msg: String, @Transient open val data: JsonElement?)