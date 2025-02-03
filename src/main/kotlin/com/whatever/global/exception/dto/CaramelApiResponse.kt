package com.whatever.global.exception.dto

import com.whatever.global.exception.common.CaramelExceptionCode

data class CaramelApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorResponse?,
) {
    companion object {
        fun succeed(): CaramelApiResponse<*> {
            return CaramelApiResponse(
                success = true,
                data = null,
                error = null,
            )
        }

        fun <T> succeed(data: T): CaramelApiResponse<T> {
            return CaramelApiResponse(
                success = true,
                data = data,
                error = null,
            )
        }

        fun failed(
            message: String?,
            caramelExceptionCode: CaramelExceptionCode
        ): CaramelApiResponse<*> {
            return CaramelApiResponse(
                success = false,
                data = null,
                error = ErrorResponse(
                    code = caramelExceptionCode.code,
                    message = caramelExceptionCode.message,
                    detailMessage = message,
                )
            )
        }
    }
}
