package com.whatever.global.exception.dto

import com.whatever.global.exception.common.CaramelExceptionCode

data class ErrorResponse(
    val code: String,
    val message: String,
    val detailMessage: String?,
) {
    companion object {
        fun of(errorCode: CaramelExceptionCode, detailMessage: String?): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
                detailMessage = detailMessage
            )
        }
    }
}
