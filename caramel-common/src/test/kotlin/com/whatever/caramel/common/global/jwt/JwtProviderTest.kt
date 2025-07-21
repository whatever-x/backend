package com.whatever.caramel.common.global.jwt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.jsonwebtoken.Jwts
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class JwtProviderTest {

    private lateinit var jwtProvider: JwtProvider
    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        val testProperties = JwtProperties(
            secretKeyStr = "a-very-long-and-secure-secret-key-for-testing-purpose-only-at-least-256-bits", // HS512는 512비트(64바이트) 이상을 권장
            accessExpirationSec = 3600L,
            refreshExpirationSec = 86400L,
            issuer = "test-issuer"
        )
        // 생성한 객체들을 사용하여 JwtProvider 인스턴스를 직접 생성
        jwtProvider = JwtProvider(testProperties, objectMapper)
    }

    @DisplayName("jwt를 claims와 함께 HS512로 서명하여 생성한다.")
    @Test
    fun createJwt() {
        // given
        val claims = mutableMapOf<String, String>()
        claims["key1"] = "val1"
        claims["key2"] = "val2"

        // when
        val jwt = jwtProvider.createJwt(
            subject = "test",
            expirationSec = 1L,  // 토큰 만료 검증은 created에서 진행 X
            claims = claims
        )

        // then
        val parts = jwt.split(".")
        assertThat(parts.size).isEqualTo(3)

        val decoder = Base64.getUrlDecoder()
        val headerMap: Map<String, Any> = objectMapper.readValue(decoder.decode(parts[0]))
        val payloadMap: Map<String, Any> = objectMapper.readValue(decoder.decode(parts[1]))

        assertThat(headerMap["alg"]).isEqualTo(Jwts.SIG.HS512.id)
        assertThat(payloadMap["sub"]).isEqualTo("test")
        assertThat(payloadMap["key1"]).isEqualTo(claims["key1"])
        assertThat(payloadMap["key2"]).isEqualTo(claims["key2"])
    }
}
