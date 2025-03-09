package com.whatever.global.security.util

import com.whatever.domain.user.model.UserStatus
import com.whatever.global.security.exception.AuthenticationException
import com.whatever.global.security.exception.SecurityExceptionCode
import com.whatever.global.security.principal.CaramelUserDetails
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {

    @JvmStatic
    fun getCurrentUserStatus(): UserStatus {
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
                detailMessage = "인증 정보가 존재하지 않습니다. 재로그인이 필요합니다."
            )

        return authentication.principal as? CaramelUserDetails ?: throw AuthenticationException(
            errorCode = SecurityExceptionCode.UNAUTHORIZED,
            detailMessage = "인증 정보가 존재하지 않습니다. 재로그인이 필요합니다."
        )
    }

    @JvmStatic
    private fun getAuthentication(): Authentication {
        return SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationException(SecurityExceptionCode.AUTHENTICATION_NOT_FOUND)
    }
}