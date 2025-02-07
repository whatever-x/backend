package com.whatever.domain.auth.service

import com.whatever.config.properties.JwtProperties
import com.whatever.global.jwt.JwtProvider
import io.jsonwebtoken.InvalidClaimException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component

@Component
class JwtHelper(
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
) {
    companion object {
        private const val USER_ID_CLAIM_KEY = "userId"
    }

    // TODO(준용) accessToken에 넣을 User 정보 Claim 상의 후 DTO로 전환
    fun createAccessToken(userId: Long): String {
        val claims = mutableMapOf<String, String>()
        claims[USER_ID_CLAIM_KEY] = userId.toString()

        return jwtProvider.createJwt(
            subject = "access",
            expirationSec = jwtProperties.accessExpirationSec,
            claims = claims,
        )
    }

    fun createRefreshToken(): String {
        return jwtProvider.createJwt(
            subject = "refresh",
            expirationSec = jwtProperties.refreshExpirationSec,
        )
    }

    // TODO(준용) User 정보 DTO 반환으로 전환
    fun parseAccessToken(token: String): Long {
        val jwt = jwtProvider.parseJwt(
            jwtParser = getJwtParser(),
            token = token,
        )

        // TODO(준용) CustomException으로 변경
        val userId = jwt.payload[USER_ID_CLAIM_KEY] ?: throw IllegalArgumentException("AccessToken이 아닙니다.")
        return userId as Long
    }

    fun isValidJwt(token: String): Boolean {
        return try {
            jwtProvider.parseJwt(
                jwtParser = getJwtParser(),
                token = token
            )
            true
        } catch (e: RuntimeException) {  // TODO(준용) CustomException으로 변경
            false
        }
    }

    private fun getJwtParser(): JwtParser {
        try {
            return Jwts.parser()
                .requireIssuer(jwtProperties.issuer)
                .verifyWith(jwtProperties.secretKey)
                .build()
        } catch (e: InvalidClaimException) {  // TODO(준용) CustomException으로 변경
            throw IllegalArgumentException("JWT의 필수 클레임이 누락되었거나 올바르지 않습니다.")
        }
    }

}