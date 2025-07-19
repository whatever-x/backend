package com.whatever.global.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.jsonwebtoken.Jwts
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.Base64

@ActiveProfiles("test")
@SpringBootTest
class JwtProviderTest @Autowired constructor(
    private val jwtProvider: JwtProvider,
    private val objectMapper: ObjectMapper,
) {

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
