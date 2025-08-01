package com.whatever.caramel.security.filter

import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.AUTH_JWT_HEADER
import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.jwt.JwtHelper
import com.whatever.caramel.common.global.jwt.JwtHelper.Companion.BEARER_TYPE
import com.whatever.caramel.domain.auth.repository.AuthRedisRepository
import com.whatever.caramel.domain.user.service.UserService
import com.whatever.caramel.security.exception.AccessDeniedException
import com.whatever.caramel.security.exception.SecurityExceptionCode
import com.whatever.caramel.security.principal.CaramelUserDetails
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
    private val authRedisRepository: AuthRedisRepository,
    private val userService: UserService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val accessToken = extractAccessToken(request)

        if (accessToken != null) {
            if (isBlacklisted(accessToken)) {
                throw AccessDeniedException(
                    errorCode = SecurityExceptionCode.BLACK_LISTED_TOKEN,
                    errorUi = ErrorUi.Toast("알 수 없는 오류가 발생했습니다.")
                )
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
        val userVo = userService.getUserWithCouple(userId) ?: return null

        val userDetails = CaramelUserDetails(
            userId = userVo.id,
            status = userVo.userStatus.name,
            coupleId = userVo.coupleId,
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
