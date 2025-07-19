package com.whatever

import global.exception.ErrorUi
import org.springframework.http.ResponseEntity

abstract class CaramelControllerAdvice {

    fun createExceptionResponse(
        errorCode: CaramelExceptionCode,
        errorUi: ErrorUi,
    ): ResponseEntity<CaramelApiResponse<*>> {
        return ResponseEntity
            .status(errorCode.status)
            .body(
                CaramelApiResponse.failed(
                    code = errorCode,
                    errorUi = errorUi,
                )
            )
    }

    fun createExceptionResponse(
        caramelException: CaramelException,
    ): ResponseEntity<CaramelApiResponse<*>> {
        return ResponseEntity
            .status(caramelException.errorCode.status)
            .body(
                CaramelApiResponse.failed(
                    code = caramelException.errorCode,
                    errorUi = caramelException.errorUi,
                )
            )
    }
}
