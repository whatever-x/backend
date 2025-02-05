package com.whatever.global.exception.common

import com.whatever.global.exception.dto.CaramelApiResponse
import org.springframework.http.ResponseEntity

abstract class CaramelControllerAdvice {

    fun createExceptionResponse(
        errorCode: CaramelExceptionCode,
        debugMessage: String? = null,
    ): ResponseEntity<CaramelApiResponse<*>> {
        return ResponseEntity
            .status(errorCode.status)
            .body(
                CaramelApiResponse.failed(
                    code = errorCode.code,
                    message = errorCode.message,
                    debugMessage = debugMessage
                )
            )
    }

}
