package com.whatever.caramel.common.advice

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException
import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import com.whatever.caramel.common.response.CaramelApiResponse
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
