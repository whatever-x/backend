package com.whatever.domain.auth.service

import com.whatever.config.properties.JwtProperties
import com.whatever.global.jwt.JwtProvider
import com.whatever.global.jwt.exception.CaramelJwtException
import com.whatever.global.jwt.exception.JwtExceptionCode
import com.whatever.global.jwt.exception.JwtMissingClaimException
import io.jsonwebtoken.*
import org.springframework.stereotype.Component

@Component
class JwtHelper(
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
) {
    companion object {
        private const val USER_ID_CLAIM_KEY = "userId"
        private const val ACCESS_SUBJECT_NAME = "access"
        private const val REFRESH_SUBJECT_NAME = "refresh"
    }

    // TODO(준용) accessToken에 넣을 User 정보 Claim 상의 후 DTO로 전환
    fun createAccessToken(userId: Long): String {
        val claims = mutableMapOf<String, String>()
        claims[USER_ID_CLAIM_KEY] = userId.toString()

        return jwtProvider.createJwt(
            subject = ACCESS_SUBJECT_NAME,
            expirationSec = jwtProperties.accessExpirationSec,
            claims = claims,
        )
    }

    fun createRefreshToken(): String {
        return jwtProvider.createJwt(
            subject = REFRESH_SUBJECT_NAME,
            expirationSec = jwtProperties.refreshExpirationSec,
        )
    }

    // TODO(준용) User 정보 DTO 반환으로 전환
    fun parseAccessToken(token: String): Long {
        val jwt = jwtProvider.parseJwt(
            jwtParser = getJwtParser(),
            token = token,
        )

        validateAccessToken(jwt)

        val userId = jwt.payload[USER_ID_CLAIM_KEY]?.toString()
            ?: throw JwtMissingClaimException(
                errorCode = JwtExceptionCode.MISSING_CLAIM,
                detailMessage = "AccessToken에서 User 정보를 찾을 수 없습니다."
            )
        return userId.toLong()
    }

    private fun validateAccessToken(jwt: Jws<Claims>) {
        val subject = jwt.payload.subject
            ?: throw JwtMissingClaimException(
                errorCode = JwtExceptionCode.UNSUPPORTED,
                detailMessage = "subject 정보가 없습니다. 지원하지 않는 JWT입니다."
            )

        if (subject != ACCESS_SUBJECT_NAME) {
            throw JwtMissingClaimException(
                errorCode = JwtExceptionCode.MISSING_CLAIM,
                detailMessage = "AccessToken이 아닌 JWT입니다. 용도: $subject"
            )
        }
    }

    fun isValidJwt(token: String): Boolean {
        return try {
            jwtProvider.parseJwt(
                jwtParser = getJwtParser(),
                token = token
            )
            true
        } catch (e: CaramelJwtException) {
            false
        }
    }

    fun getUserId(token: String): Long {
        return jwtProvider.getUnsecuredPayload(token)["userId"]?.toLong()
            ?: throw IllegalArgumentException("userid require not null")
    }

    private fun getJwtParser(): JwtParser {
        return Jwts.parser()
            .requireIssuer(jwtProperties.issuer)
            .verifyWith(jwtProperties.secretKey)
            .build()
    }

}