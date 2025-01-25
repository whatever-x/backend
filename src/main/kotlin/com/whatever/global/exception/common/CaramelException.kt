package com.whatever.global.exception.common

abstract class CaramelException(
    val errorCode: CaramelExceptionCode,
    val detailMessage: String? = null,
) : RuntimeException(errorCode.message)
