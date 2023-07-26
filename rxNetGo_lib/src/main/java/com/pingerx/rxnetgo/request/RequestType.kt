package com.pingerx.rxnetgo.request

/**
 * 请求类型，暂时只支持普通的GET和POST请求
 */
enum class RequestType {

    /**
     * GET请求
     */
    GET,

    /**
     * POST请求
     */
    POST,
    PUT,
    DELETE
}
