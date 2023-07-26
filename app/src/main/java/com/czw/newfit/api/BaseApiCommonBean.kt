package com.czw.newfit.api

import com.google.gson.JsonElement

/**
 * 最上层的Api数据结构
 * data字段有可能返回null
 */
data class BaseApiCommonBean(override var code: Int, override val msg: String, override val data: JsonElement?) : BaseApiBean(code, msg, data)