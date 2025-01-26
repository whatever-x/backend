package com.whatever.global.exception

import com.whatever.global.exception.common.CaramelException
import com.whatever.global.exception.common.CaramelControllerAdvice
import com.whatever.global.exception.dto.ExceptionResponse
import jakarta.servlet.ServletException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalControllerAdvice : CaramelControllerAdvice() {

    // TODO: 로깅처리 필수

    @ExceptionHandler(CaramelException::class)
    fun handleCaramelException(e: CaramelException): ResponseEntity<ExceptionResponse> {
        return createExceptionResponse(e.errorCode, e.detailMessage)
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        HandlerMethodValidationException::class,
        ConstraintViolationException::class,
    )
    fun handleArgumentValidationException(e: Exception): ResponseEntity<ExceptionResponse> {
        val detailMessage =
            when (e) {
                is BindException -> e.bindingResult.fieldErrors.joinToString(", ") {
                        "${it.field} : ${it.defaultMessage}"
                    }

                is ConstraintViolationException -> e.constraintViolations.joinToString(", ") {
                    "${it.propertyPath.last().name} : ${it.message}"
                }

                else -> "처리할 수 없는 에러가 발생했습니다. 관리자에게 문의해주세요."
            }
        return createExceptionResponse(
            GlobalExceptionCode.ARGS_VALIDATION_FAILED,
            detailMessage
        )
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class
    )
    fun handleMethodArgumentTypeMismatchException(e: Exception): ResponseEntity<ExceptionResponse> {
        return createExceptionResponse(
            GlobalExceptionCode.ARGS_TYPE_MISSMATCH,
            null
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleApplicationException(e: Exception): ResponseEntity<ExceptionResponse> {
        return createExceptionResponse(GlobalExceptionCode.UNKNOWN)
    }

    @ExceptionHandler(
        NoResourceFoundException::class,
        HttpRequestMethodNotSupportedException::class
    )
    fun handleNoResourceFoundException(e: ServletException): ResponseEntity<ExceptionResponse> {
        return createExceptionResponse(GlobalExceptionCode.NO_RESOURCE)
    }
}
