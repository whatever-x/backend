package com.whatever.domain.auth.service

import com.whatever.caramel.common.global.jwt.JwtProperties
import com.whatever.caramel.common.global.jwt.JwtProvider
import com.whatever.caramel.common.global.jwt.exception.CaramelJwtException
import com.whatever.caramel.common.global.jwt.exception.JwtExceptionCode
import com.whatever.caramel.common.global.jwt.exception.JwtMissingClaimException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtHelper(
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
) {

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

    fun extractJti(token: String): String {
        val jwt = jwtProvider.parseJwt(
            jwtParser = getJwtParser(),
            token = token,
        )

        return jwt.payload.id
            ?: throw JwtMissingClaimException(
                errorCode = JwtExceptionCode.MISSING_JTI,
                detailMessage = "AccessToken에서 token id 정보를 찾을 수 없습니다."
            )
    }

    fun extractExpDate(token: String): Date {
        val jwt = jwtProvider.parseJwt(
            jwtParser = getJwtParser(),
            token = token,
        )

        return jwt.payload.expiration
            ?: throw JwtMissingClaimException(
                errorCode = JwtExceptionCode.MISSING_JTI,
                detailMessage = "AccessToken에서 token id 정보를 찾을 수 없습니다."
            )
    }

    fun extractUserId(token: String): Long {
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

    fun extractUserIdIgnoringSignature(token: String): Long {
        return jwtProvider.getUnsecuredPayload(token)["userId"]?.toLong()
            ?: throw JwtMissingClaimException(
                errorCode = JwtExceptionCode.MISSING_CLAIM,
                detailMessage = "Missing 'userId' claim. Please check your token."
            )
    }

    private fun getJwtParser(): JwtParser {
        return Jwts.parser()
            .requireIssuer(jwtProperties.issuer)
            .verifyWith(jwtProperties.secretKey)
            .build()
    }

    companion object {
        private const val USER_ID_CLAIM_KEY = "userId"
        private const val ACCESS_SUBJECT_NAME = "access"
        private const val REFRESH_SUBJECT_NAME = "refresh"
        const val BEARER_TYPE = "Bearer "
    }
}
