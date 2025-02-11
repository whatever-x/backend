package com.whatever.global.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.global.security.exception.SecurityExceptionCode
import com.whatever.global.security.util.setExceptionResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {  }

@Component
class CaramelAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.error(authException) { request.requestURI }
        response.setExceptionResponse(
            errorCode = SecurityExceptionCode.UNAUTHORIZED,
            detailMessage = "인증에 실패했습니다. 접근 API: ${request.requestURI}",
            objectMapper = objectMapper
        )
    }
}