package com.whatever.global.security.filter

import com.whatever.domain.auth.service.JwtHelper
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.principal.CaramelUserDetails
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtHelper: JwtHelper,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {

    companion object {
        private const val AUTH_JWT_HEADER = "Authorization"
        private const val BEARER_TYPE = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = extractAccessToken(request)
        if (accessToken != null) {
            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = getAuthentication(accessToken)
            SecurityContextHolder.setContext(context)
        }

        filterChain.doFilter(request, response)
    }

    private fun getAuthentication(accessToken: String): UsernamePasswordAuthenticationToken? {
        val userId = jwtHelper.parseAccessToken(accessToken)
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

    private fun extractAccessToken(request: HttpServletRequest): String? {
        val rawToken: String? = request.getHeader(AUTH_JWT_HEADER)
        if (rawToken.isNullOrBlank() || !rawToken.startsWith(BEARER_TYPE)) {
            return null
        }
        return rawToken.substring(BEARER_TYPE.length)
    }
}