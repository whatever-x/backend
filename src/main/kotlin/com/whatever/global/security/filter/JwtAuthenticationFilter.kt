package com.whatever.global.security.filter

import com.whatever.domain.auth.repository.AuthRedisRepository
import com.whatever.domain.auth.service.JwtHelper
import com.whatever.domain.auth.service.JwtHelper.Companion.BEARER_TYPE
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.constants.CaramelHttpHeaders.AUTH_JWT_HEADER
import com.whatever.global.security.exception.AccessDeniedException
import com.whatever.global.security.exception.SecurityExceptionCode
import com.whatever.global.security.principal.CaramelUserDetails
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtHelper: JwtHelper,
    private val userRepository: UserRepository,
    private val authRedisRepository: AuthRedisRepository,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = extractAccessToken(request)

        if (accessToken != null) {
            if (isBlacklisted(accessToken)) {
                throw AccessDeniedException(SecurityExceptionCode.BLACK_LISTED_TOKEN)
            }
            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = getAuthentication(accessToken)
            SecurityContextHolder.setContext(context)
        }

        filterChain.doFilter(request, response)
    }

    private fun isBlacklisted(accessToken: String): Boolean {
        val jti = jwtHelper.extractJti(accessToken)
        return authRedisRepository.isJtiBlacklisted(jti)
    }

    private fun getAuthentication(accessToken: String): UsernamePasswordAuthenticationToken? {
        val userId = jwtHelper.extractUserId(accessToken)
        val user = userRepository.findByIdWithCouple(userId) ?: return null

        val userDetails = CaramelUserDetails(
            userId = user.id,
            status = user.userStatus,
            coupleId = user.couple?.id ?: 0L
        )

        return UsernamePasswordAuthenticationToken(
            userDetails,
            userDetails.password,
            userDetails.authorities
        )
    }

    /**
     * Request에서 Access Token을 추출하고 Bearer prefix를 제거하는 함수
     * @param request 서블릿 request
     * @return Bearer가 제거된 온전한 access token
     */
    private fun extractAccessToken(request: HttpServletRequest): String? {
        val rawToken: String? = request.getHeader(AUTH_JWT_HEADER)
        if (rawToken.isNullOrBlank() || !rawToken.startsWith(BEARER_TYPE)) {
            return null
        }
        return rawToken.substring(BEARER_TYPE.length)
    }
}