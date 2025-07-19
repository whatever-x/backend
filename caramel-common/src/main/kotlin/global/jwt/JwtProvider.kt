package global.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.whatever.config.properties.JwtProperties
import com.whatever.global.jwt.exception.CaramelJwtException
import com.whatever.global.jwt.exception.JwtExceptionCode
import com.whatever.global.jwt.exception.JwtExpiredException
import com.whatever.global.jwt.exception.JwtMalformedException
import com.whatever.global.jwt.exception.JwtSecurityException
import com.whatever.global.jwt.exception.JwtSignatureException
import com.whatever.global.jwt.exception.JwtUnsupportedException
import com.whatever.util.DateTimeUtil
import com.whatever.util.toDate
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SecurityException
import io.jsonwebtoken.security.SignatureException
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.UUID

private val logger = KotlinLogging.logger { }

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
            .id(UUID.randomUUID().toString())
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
        } catch (e: MalformedJwtException) {
            logger.error(e) { "MalformedJwtException 발생 - 토큰 형식이 잘못되었습니다. 토큰: ${token}" }
            throw JwtMalformedException(JwtExceptionCode.PARSE_FAILED)
        } catch (e: SignatureException) {
            logger.error(e) { "SignatureException 발생 - JWT 서명 검증에 실패했습니다. 토큰: ${token}" }
            throw JwtSignatureException(JwtExceptionCode.SIGNATURE_INVALID)
        } catch (e: SecurityException) {
            logger.error(e) { "SecurityException 발생 - JWT 암호 해독에 실패했습니다. 토큰: ${token}" }
            throw JwtSecurityException(JwtExceptionCode.SECURITY_FAILURE)
        } catch (e: ExpiredJwtException) {
            logger.error(e) { "ExpiredJwtException 발생 - JWT가 만료되었습니다. 종류: ${e.claims.subject} 만료시간: ${e.claims.expiration}" }
            throw JwtExpiredException(JwtExceptionCode.EXPIRED)
        } catch (e: UnsupportedJwtException) {
            logger.error(e) { "UnsupportedJwtException 발생 - 지원되지 않는 JWT 형식입니다. 토큰: ${token}" }
            throw JwtUnsupportedException(JwtExceptionCode.PARSE_FAILED)
        } catch (e: JwtException) {
            logger.error(e) { "JwtException 발생 - JWT 파싱 또는 검증 중 오류가 발생했습니다. 토큰: ${token}" }
            throw CaramelJwtException(JwtExceptionCode.PARSE_FAILED)
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

    fun getUnsecuredPayload(token: String): Map<String, String> {
        val jwtChunk = getJwtChunk(token)
        val jwtHeader = Base64.getDecoder().decode(jwtChunk[1])
        return objectMapper.readValue(jwtHeader)
    }

    private fun getJwtChunk(token: String): List<String> {
        val jwtChunk = token.split(Regex.fromLiteral("."))
        if (jwtChunk.size < 3) {
            throw JwtMalformedException(JwtExceptionCode.MALFORMED)
        }
        return jwtChunk
    }
}
