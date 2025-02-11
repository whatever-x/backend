package com.whatever.global.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.domain.user.dto.UserStatus
import com.whatever.global.security.exception.SecurityExceptionCode
import com.whatever.global.security.util.getCurrentUserAuthorities
import com.whatever.global.security.util.getCurrentUserId
import com.whatever.global.security.util.setExceptionResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {  }

@Component
class CaramelAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {

        val userAuthorities = getCurrentUserAuthorities()
        logger.error(accessDeniedException) {
            "API 접근 실패. UserId: ${getCurrentUserId()}, Authorities: ${userAuthorities}, API: ${request.requestURI}"
        }

        val detailMessage = when {
            userAuthorities.hasAuthority(UserStatus.NEW) -> "신규 유저이므로 초기 정보를 입력해야합니다."
            else -> "해당하는 API에 접근할 권한이 없습니다. 접근 API: ${request.requestURI}"
        }

        response.setExceptionResponse(
            errorCode = SecurityExceptionCode.FORBIDDEN,
            detailMessage = detailMessage,
            objectMapper = objectMapper
        )
    }
}

private fun Set<GrantedAuthority>.hasAuthority(userStatus: UserStatus): Boolean {
    return any { it.authority.endsWith(userStatus.name) }
}
