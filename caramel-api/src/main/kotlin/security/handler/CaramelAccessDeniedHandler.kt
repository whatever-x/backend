package com.whatever.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.SecurityUtil.getCurrentUserAuthorities
import com.whatever.SecurityUtil.getCurrentUserId
import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.domain.user.model.UserStatus
import com.whatever.security.exception.SecurityExceptionCode
import com.whatever.security.util.setExceptionResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

@Component
class CaramelAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {

        val userAuthorities = getCurrentUserAuthorities()
        val detailMessage = when {
            userAuthorities.hasAuthority(UserStatus.NEW) -> "신규 유저이므로 초기 정보를 입력해야합니다."
            else -> "해당하는 API에 접근할 권한이 없습니다."
        }
        logger.error(accessDeniedException) {
            "API 접근 실패. ${detailMessage} UserId: ${getCurrentUserId()}, Authorities: ${userAuthorities}, API: ${request.requestURI}"
        }


        response.setExceptionResponse(
            errorCode = SecurityExceptionCode.FORBIDDEN,
            errorUi = ErrorUi.Toast("접근할 수 없는 작업이에요"),
            objectMapper = objectMapper
        )
    }
}

private fun Set<GrantedAuthority>.hasAuthority(userStatus: UserStatus): Boolean {
    return any { it.authority.endsWith(userStatus.name) }
}
