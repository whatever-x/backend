package com.whatever.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.GlobalExceptionCode
import com.whatever.caramel.common.global.jwt.exception.CaramelJwtException
import com.whatever.security.exception.CaramelSecurityException
import com.whatever.security.util.setExceptionResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtExceptionFilter(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: CaramelJwtException) {
            response.setExceptionResponse(
                errorCode = e.errorCode,
                errorUi = e.errorUi,
                objectMapper = objectMapper
            )
        } catch (e: CaramelSecurityException) {
            response.setExceptionResponse(
                errorCode = e.errorCode,
                errorUi = e.errorUi,
                objectMapper = objectMapper
            )
        } catch (e: Exception) {
            response.setExceptionResponse(
                errorCode = GlobalExceptionCode.UNKNOWN,
                errorUi = ErrorUi.Dialog("로그인을 하지 못했어요.\n다시 한 번 시도해주세요."),
                objectMapper = objectMapper
            )
        }
    }
}
