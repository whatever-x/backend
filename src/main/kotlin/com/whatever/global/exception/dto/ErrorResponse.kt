package com.whatever.global.exception.dto

import com.whatever.global.exception.common.CaramelExceptionCode

data class ErrorResponse(
    val code: String,
    val message: String,
    val debugMessage: String?,
) {
    companion object {
        fun of(errorCode: CaramelExceptionCode, debugMessage: String?): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
                debugMessage = debugMessage
            )
        }
    }
}
