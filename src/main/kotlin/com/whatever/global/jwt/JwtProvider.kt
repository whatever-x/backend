package com.whatever.global.jwt

import com.whatever.config.properties.JwtProperties
import com.whatever.util.DateTimeUtil
import com.whatever.util.toDate
import io.jsonwebtoken.*
import org.springframework.stereotype.Component

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {

    // TODO(준용) accessToken에 넣을 Claim 상의 후 DTO로 전환
    fun createAccessToken(userId: Long): String {
        val claims = mutableMapOf<String, String>()
        claims["userId"] = userId.toString()

        return createJwt(
            subject = "access",
            expirationSec = jwtProperties.accessExpirationSec,
            claims = claims,
        )
    }

    fun createRefreshToken(): String {
        return createJwt(
            subject = "refresh",
            expirationSec = jwtProperties.refreshExpirationSec,
        )
    }

    fun createJwt(
        subject: String,
        expirationSec: Long,
        claims: Map<String, String> = emptyMap(),
    ): String {
        val now = DateTimeUtil.zonedNow()
        val issueDate = now.toDate()
        val expiredDate = now.plusSeconds(expirationSec).toDate()

        return Jwts.builder()
            .claims(claims)
            .issuer(jwtProperties.issuer)
            .subject(subject)
            .issuedAt(issueDate)
            .expiration(expiredDate)
            .signWith(jwtProperties.secretKey)
            .compact()
    }

    fun isValidJwt(token: String): Boolean {
        return try {
            parseJwt(token)
            true
        } catch (e: RuntimeException) {
            false
        }
    }

    private fun parseJwt(token: String): Jws<Claims> {
        try {
            return Jwts.parser()
                .requireIssuer(jwtProperties.issuer)
                .verifyWith(jwtProperties.secretKey)
                .build()
                .parseSignedClaims(token)

            // TODO(준용) CustomException으로 변경
        } catch (e: InvalidClaimException) {
            throw IllegalArgumentException("JWT의 필수 클레임이 누락되었거나 올바르지 않습니다.")
        } catch (e: UnsupportedJwtException) {
            throw IllegalArgumentException("서명되지 않았거나 지원되지 않는 JWT 형식입니다.")
        } catch (e: JwtException) {
            throw IllegalArgumentException("JWT를 파싱하거나 검증하는 과정에서 오류가 발생했습니다.")
        }
    }

}
