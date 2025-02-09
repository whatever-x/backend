package com.whatever.global.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.whatever.config.properties.JwtProperties
import com.whatever.util.DateTimeUtil
import com.whatever.util.toDate
import io.jsonwebtoken.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
    private val objectMapper: ObjectMapper,
) {
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

    fun parseJwt(jwtParser: JwtParser, token: String): Jws<Claims> {
        try {
            return jwtParser.parseSignedClaims(token)

            // TODO(준용) CustomException으로 변경
        } catch (e: UnsupportedJwtException) {
            throw IllegalArgumentException("서명되지 않았거나 지원되지 않는 JWT 형식입니다.")
        } catch (e: JwtException) {
            throw IllegalArgumentException("JWT를 파싱하거나 검증하는 과정에서 오류가 발생했습니다.")
        }
    }

    /**
     * 서명을 검증하지 않아 검증 용도로 사용하면 안됩니다.
     */
    fun getUnsecuredHeader(token: String): Map<String, String> {
        val jwtChunk = getJwtChunk(token)
        val jwtHeader = Base64.getDecoder().decode(jwtChunk[0])  // header를 디코딩하여 kid 추출
        return objectMapper.readValue(jwtHeader)
    }

    private fun getJwtChunk(token: String): List<String> {
        val jwtChunk = token.split(Regex.fromLiteral("."))
        if (jwtChunk.size < 3) {
            // TODO(준용) CustomException으로 변경
            throw IllegalArgumentException("올바르지 않은 JWT 형식입니다.")
        }
        return jwtChunk
    }

}
