package com.whatever.global.exception.common

import com.whatever.global.exception.ErrorUiType
import com.whatever.global.exception.dto.CaramelApiResponse
import org.springframework.http.ResponseEntity

abstract class CaramelControllerAdvice {

    fun createExceptionResponse(
        errorCode: CaramelExceptionCode,
        debugMessage: String? = null,
        overrideErrorUiType: ErrorUiType? = null,
    ): ResponseEntity<CaramelApiResponse<*>> {
        return ResponseEntity
            .status(errorCode.status)
            .body(
                CaramelApiResponse.failed(
                    code = errorCode,
                    debugMessage = debugMessage,
                    overrideErrorUiType = overrideErrorUiType,
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
                    debugMessage = caramelException.detailMessage,
                    overrideErrorUiType = caramelException.overrideErrorUiType,
                )
            )
    }

}
