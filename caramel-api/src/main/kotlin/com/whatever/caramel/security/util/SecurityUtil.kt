package com.whatever.caramel.security.util

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.security.exception.AuthenticationException
import com.whatever.caramel.security.exception.SecurityExceptionCode
import com.whatever.caramel.security.principal.CaramelUserDetails
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {

    @JvmStatic
    fun getCurrentUserCoupleId(): Long {
        return getUserDetails().coupleId
    }

    @JvmStatic
    fun getCurrentUserStatus(): String {
        return getUserDetails().status
    }

    @JvmStatic
    fun getCurrentUserAuthorities(): Set<GrantedAuthority> {
        return getUserDetails().authorities
    }

    @JvmStatic
    fun getCurrentUserId(): Long {
        return getUserDetails().userId
    }

    @JvmStatic
    private fun getUserDetails(): CaramelUserDetails {
        val authentication = getAuthentication()
            .takeIf { it.isAuthenticated }
            ?: throw AuthenticationException(
                errorCode = SecurityExceptionCode.UNAUTHORIZED,
                errorUi = ErrorUi.Toast("인증 정보를 찾을 수 없어요. 다시 로그인이 필요해요."),
            )

        return authentication.principal as? CaramelUserDetails ?: throw AuthenticationException(
            errorCode = SecurityExceptionCode.UNAUTHORIZED,
            errorUi = ErrorUi.Toast("인증 정보를 찾을 수 없어요. 다시 로그인이 필요해요."),
        )
    }

    @JvmStatic
    private fun getAuthentication(): Authentication {
        return SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationException(
                errorCode = SecurityExceptionCode.AUTHENTICATION_NOT_FOUND,
                errorUi = ErrorUi.Toast("인증 정보를 찾을 수 없어요. 다시 로그인이 필요해요."),
            )
    }
}
