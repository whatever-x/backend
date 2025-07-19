package com.whatever.config.openfeign

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.whatever.global.exception.externalserver.kakao.KakaoBadRequestException
import com.whatever.global.exception.externalserver.kakao.KakaoForbiddenException
import com.whatever.global.exception.externalserver.kakao.KakaoServerException
import com.whatever.global.exception.externalserver.kakao.KakaoServerExceptionCode
import com.whatever.global.exception.externalserver.kakao.KakaoServiceUnavailableException
import com.whatever.global.exception.externalserver.kakao.KakaoUnauthorizedException
import feign.Request
import feign.RequestTemplate
import feign.Response
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.nio.charset.StandardCharsets

class KapiErrorDecoderTest {

    private val kapiErrorDecoder = KapiErrorDecoder()
    private val objectMapper = jacksonObjectMapper()

    @DisplayName("HTTP 400 - 잘못된 요청인 경우 KakaoBadRequestException 예외가 발생한다.")
    @Test
    fun decode_ThrowsKakaoBadRequestException() {
        // given
        val errorCode = -2
        val response = createDummyResponse(HttpStatus.BAD_REQUEST, errorCode, "Invalid parameter")
        // when, then:
        assertThatThrownBy { kapiErrorDecoder.decode("testMethod", response) }
            .isInstanceOf(KakaoBadRequestException::class.java)
            .hasMessage(KakaoServerExceptionCode.fromKakaoErrorCode(errorCode).message)
    }

    @DisplayName("HTTP 401 - 인증 실패인 경우 KakaoUnauthorizedException 예외가 발생한다.")
    @Test
    fun decode_ThrowsKakaoUnauthorizedException() {
        // given
        val errorCode = -401
        val response = createDummyResponse(HttpStatus.UNAUTHORIZED, errorCode, "Unauthorized access")
        // when, then:
        assertThatThrownBy { kapiErrorDecoder.decode("testMethod", response) }
            .isInstanceOf(KakaoUnauthorizedException::class.java)
            .hasMessage(KakaoServerExceptionCode.fromKakaoErrorCode(errorCode).message)
    }

    @DisplayName("HTTP 403 - 접근 거부인 경우 KakaoForbiddenException 예외가 발생한다.")
    @Test
    fun decode_ThrowsKakaoForbiddenException() {
        // given
        val errorCode = -4
        val response = createDummyResponse(HttpStatus.FORBIDDEN, errorCode, "Access denied")
        // when, then:
        assertThatThrownBy { kapiErrorDecoder.decode("testMethod", response) }
            .isInstanceOf(KakaoForbiddenException::class.java)
            .hasMessage(KakaoServerExceptionCode.fromKakaoErrorCode(errorCode).message)
    }

    @DisplayName("HTTP 503 - 서비스 이용 불가인 경우 KakaoServiceUnavailableException 예외가 발생한다.")
    @Test
    fun decode_ThrowsKakaoServiceUnavailableException() {
        // given
        val errorCode = -9798
        val response = createDummyResponse(HttpStatus.SERVICE_UNAVAILABLE, errorCode, "Service unavailable")
        // when, then:
        assertThatThrownBy { kapiErrorDecoder.decode("testMethod", response) }
            .isInstanceOf(KakaoServiceUnavailableException::class.java)
            .hasMessage(KakaoServerExceptionCode.fromKakaoErrorCode(errorCode).message)
    }

    @DisplayName("HTTP 500 - 매핑되지 않은 오류 상태인 경우 KakaoServerException 예외가 발생한다.")
    @Test
    fun decode_ThrowsKakaoServerException() {
        // given
        val unexpectedErrorCode = -9999
        val response =
            createDummyResponse(HttpStatus.INTERNAL_SERVER_ERROR, unexpectedErrorCode, "Internal server error")
        // when, then:
        assertThatThrownBy { kapiErrorDecoder.decode("testMethod", response) }
            .isInstanceOf(KakaoServerException::class.java)
            .hasMessage(KakaoServerExceptionCode.UNKNOWN.message)
    }

    private fun createDummyResponse(status: HttpStatus, code: Int, msg: String): Response {
        val json = objectMapper.writeValueAsString(
            mapOf(
                "code" to code,
                "msg" to msg
            )
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
