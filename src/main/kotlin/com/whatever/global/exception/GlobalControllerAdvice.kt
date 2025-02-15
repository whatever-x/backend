package com.whatever.global.exception

import com.whatever.global.exception.common.CaramelControllerAdvice
import com.whatever.global.exception.common.CaramelException
import com.whatever.global.exception.dto.CaramelApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
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

private val logger = KotlinLogging.logger {  }

@RestControllerAdvice
class GlobalControllerAdvice : CaramelControllerAdvice() {

    // TODO: 로깅처리 필수

    @ExceptionHandler(CaramelException::class)
    fun handleCaramelException(e: CaramelException): ResponseEntity<CaramelApiResponse<*>> {
        return createExceptionResponse(errorCode = e.errorCode, debugMessage = e.detailMessage)
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        HandlerMethodValidationException::class,
        ConstraintViolationException::class,
    )
    fun handleArgumentValidationException(e: Exception): ResponseEntity<CaramelApiResponse<*>> {
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
            errorCode = GlobalExceptionCode.ARGS_VALIDATION_FAILED,
            debugMessage = detailMessage
        )
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class
    )
    fun handleMethodArgumentTypeMismatchException(e: Exception): ResponseEntity<CaramelApiResponse<*>> {
        return createExceptionResponse(
            errorCode = GlobalExceptionCode.ARGS_TYPE_MISSMATCH,
            debugMessage = null
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleApplicationException(e: Exception): ResponseEntity<CaramelApiResponse<*>> {
        logger.error(e) { "예상하지 못한 예외가 발생했습니다." }
        return createExceptionResponse(errorCode = GlobalExceptionCode.UNKNOWN)
    }

    @ExceptionHandler(
        NoResourceFoundException::class,
        HttpRequestMethodNotSupportedException::class
    )
    fun handleNoResourceFoundException(e: ServletException): ResponseEntity<CaramelApiResponse<*>> {
        return createExceptionResponse(errorCode = GlobalExceptionCode.NO_RESOURCE)
    }
}
