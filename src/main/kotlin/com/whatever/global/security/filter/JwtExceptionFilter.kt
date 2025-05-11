package com.whatever.global.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.global.exception.GlobalExceptionCode
import com.whatever.global.jwt.exception.CaramelJwtException
import com.whatever.global.security.exception.AuthenticationException
import com.whatever.global.security.exception.CaramelSecurityException
import com.whatever.global.security.exception.SecurityExceptionCode
import com.whatever.global.security.util.setExceptionResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtExceptionFilter(
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: CaramelJwtException) {
            response.setExceptionResponse(
                errorCode = e.errorCode,
                detailMessage = e.detailMessage,
                objectMapper = objectMapper
            )
        } catch (e: CaramelSecurityException) {
            response.setExceptionResponse(
                errorCode = e.errorCode,
                detailMessage = e.detailMessage,
                objectMapper = objectMapper
            )
        } catch (e: Exception) {
            response.setExceptionResponse(
                errorCode = GlobalExceptionCode.UNKNOWN,
                detailMessage = "인증 과정에서 예상하지 못한 에러가 발생했습니다.",
                objectMapper = objectMapper
            )
        }
    }
}
