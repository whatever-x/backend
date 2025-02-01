package com.whatever.global.exception.dto

import com.whatever.global.exception.common.CaramelExceptionCode

data class ExceptionResponse(
    val code: String,
    val message: String,
    val detailMessage: String?,
) {
    companion object {
        fun of(errorCode: CaramelExceptionCode, detailMessage: String?): ExceptionResponse {
            return ExceptionResponse(
                code = errorCode.code,
                message = errorCode.message,
                detailMessage = detailMessage
            )
        }
    }
}
