package com.whatever.caramel.infrastructure.openfeign

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.whatever.caramel.common.global.exception.externalserver.kakao.KakaoServerException
import com.whatever.caramel.common.global.exception.externalserver.kakao.KakaoServerExceptionCode
import feign.Request
import feign.RequestTemplate
import feign.Response
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.NullSource
import org.springframework.http.HttpStatus
import java.nio.charset.StandardCharsets

class KauthErrorDecoderTest {

    private val errorDecoder = KauthErrorDecoder()
    private val objectMapper = jacksonObjectMapper()

    @DisplayName("kauth api에서 에러 코드가 있는 응답일 경우 KakaoServerException 예외가 발생한다.")
    @Test
    fun decode_ThrowsKakaoServerException() {
        // given
        val errorCode = "KOE400"
        val errorDescription = "카카오 인증 토큰이 없거나, 올바른 형식이 아닙니다."
        val response = createDummyResponse(HttpStatus.BAD_REQUEST, errorCode, errorDescription)
        // when, then:
        assertThatThrownBy { errorDecoder.decode("testMethod", response) }
            .isInstanceOf(KakaoServerException::class.java)
            .hasMessage(KakaoServerExceptionCode.fromKakaoErrorCode(errorCode).message)
    }

    @DisplayName("kauth api에서 유효하지 않은 에러 코드 올 경우 Unknown 코드인 KakaoServerException 예외가 발생한다.")
    @ParameterizedTest
    @CsvSource("???")
    @NullSource
    fun decode_ThrowsKakaoServerExceptionWithUnknownCode(errorCode: String?) {
        // given
        val errorDescription = "알 수 없는 에러입니다."
        val response = createDummyResponse(HttpStatus.BAD_REQUEST, errorCode, errorDescription)
        // when, then:
        assertThatThrownBy { errorDecoder.decode("testMethod", response) }
            .isInstanceOf(KakaoServerException::class.java)
            .hasMessage(KakaoServerExceptionCode.UNKNOWN.message)
    }

    private fun createDummyResponse(status: HttpStatus, error: String?, errorDescription: String): Response {
        val json = objectMapper.writeValueAsString(
            mapOf("error" to error, "errorDescription" to errorDescription)
        )
        val dummyRequest = Request.create(
            Request.HttpMethod.GET,
            "http://localhost.test",
            emptyMap(),
            null,
            StandardCharsets.UTF_8,
            RequestTemplate()
        )
        return Response.builder()
            .request(dummyRequest)
            .status(status.value())
            .reason("reason")
            .headers(emptyMap())
            .body(json, StandardCharsets.UTF_8)
            .build()
    }
}
