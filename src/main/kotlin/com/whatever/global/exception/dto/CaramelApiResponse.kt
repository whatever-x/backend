package com.whatever.global.exception.dto

data class CaramelApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorResponse?,
) {

    companion object {
        fun <T> succeed(data: T? = null): CaramelApiResponse<T> {
            return CaramelApiResponse(
                success = true,
                data = data,
                error = null,
            )
        }

        fun failed(
            code: String,
            message: String,
            debugMessage: String?,
        ): CaramelApiResponse<*> {
            return CaramelApiResponse(
                success = false,
                data = null,
                error = ErrorResponse(
                    code = code,
                    message = message,
                    debugMessage = debugMessage,
                )
            )
        }
    }
}

fun <T> T.succeed() = CaramelApiResponse(
    success = true,
    data = this,
    error = null,
)