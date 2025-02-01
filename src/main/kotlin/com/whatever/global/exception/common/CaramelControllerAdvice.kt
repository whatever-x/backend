package com.whatever.global.exception.common

import com.whatever.global.exception.dto.ExceptionResponse
import org.springframework.http.ResponseEntity

abstract class CaramelControllerAdvice {

    fun createExceptionResponse(
        errorCode: CaramelExceptionCode,
        detailMessage: String? = null,
    ): ResponseEntity<ExceptionResponse> {
        return ResponseEntity
            .status(errorCode.status)
            .body(ExceptionResponse.of(errorCode, detailMessage))
    }

}
