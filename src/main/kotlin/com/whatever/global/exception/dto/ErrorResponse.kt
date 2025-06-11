package com.whatever.global.exception.dto

import com.whatever.global.exception.ErrorUiType
import com.whatever.global.exception.ErrorUiType.TOAST
import com.whatever.global.exception.common.CaramelExceptionCode

data class ErrorResponse(
    val code: String,
    val message: String,
    val debugMessage: String?,
    val errorUiType: ErrorUiType,
) {
    companion object {
        fun of(
            errorCode: CaramelExceptionCode,
            debugMessage: String?,
            errorUiType: ErrorUiType = TOAST,
        ): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
                debugMessage = debugMessage,
                errorUiType = errorUiType,
            )
        }
    }
}
