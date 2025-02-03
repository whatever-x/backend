package com.whatever.global.exception.common

import com.whatever.global.exception.dto.ErrorResponse
import org.springframework.http.ResponseEntity

abstract class CaramelControllerAdvice {

    fun createExceptionResponse(
        errorCode: CaramelExceptionCode,
        detailMessage: String? = null,
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(errorCode, detailMessage))
    }

}
