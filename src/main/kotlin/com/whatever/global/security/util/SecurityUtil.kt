package com.whatever.global.security.util

import com.whatever.global.security.principal.CaramelUserDetails
import com.whatever.global.security.exception.AuthenticationException
import com.whatever.global.security.exception.SecurityExceptionCode
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

fun getCurrentUserAuthorities(): Set<GrantedAuthority> {
    return getUserDetails().authorities
}

fun getCurrentUserId(): Long {
    return getUserDetails().getUserId()
}

fun getUserDetails(): CaramelUserDetails {
    val authentication = getAuthentication()
        .takeIf { it.isAuthenticated }
        ?: throw AuthenticationException(
            errorCode = SecurityExceptionCode.UNAUTHORIZED,
            detailMessage = "인증 정보가 존재하지 않습니다. 재로그인이 필요합니다."
        )

    return authentication.principal as CaramelUserDetails
}

private fun getAuthentication(): Authentication {
    return SecurityContextHolder.getContext().authentication
        ?: throw AuthenticationException(SecurityExceptionCode.AUTHENTICATION_NOT_FOUND)
}
