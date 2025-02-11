package com.whatever.global.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.global.jwt.exception.CaramelJwtException
import com.whatever.global.security.exception.AuthenticationException
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
        } catch (e: AuthenticationException) {
            response.setExceptionResponse(
                errorCode = e.errorCode,
                detailMessage = e.detailMessage,
                objectMapper = objectMapper
            )
        }
    }
}
