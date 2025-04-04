package com.whatever.config.openfeign

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.whatever.global.exception.externalserver.kakao.KakaoBadRequestException
import com.whatever.global.exception.externalserver.kakao.KakaoForbiddenException
import com.whatever.global.exception.externalserver.kakao.KakaoServerException
import com.whatever.global.exception.externalserver.kakao.KakaoServerExceptionCode
import com.whatever.global.exception.externalserver.kakao.KakaoServerExceptionCode.UNKNOWN
import com.whatever.global.exception.externalserver.kakao.KakaoServiceUnavailableException
import com.whatever.global.exception.externalserver.kakao.KakaoUnauthorizedException
import feign.FeignException
import feign.Response
import feign.codec.Encoder
import feign.codec.ErrorDecoder
import feign.form.FormEncoder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {  }

class KakaoKapiConfig {

    @Bean
    fun formEncoder(): Encoder {
        return FormEncoder()
    }

    @Bean
    fun kapiErrorDecoder(): KapiErrorDecoder {
        return KapiErrorDecoder()
    }
}

class KapiErrorDecoder() : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response?): Exception {
        if (response == null || response.status() < 400) {
            return FeignException.errorStatus(methodKey, response)
        }

        val errorResponse = response.toObject(KapiErrorResponse::class)
        logger.error { "kapi exception. response: ${errorResponse}" }

        when (HttpStatus.valueOf(response.status())) {
            HttpStatus.BAD_REQUEST ->
                throw KakaoBadRequestException(
                    errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.code),
                    detailMessage = errorResponse.msg,
                )
            HttpStatus.UNAUTHORIZED ->
                throw KakaoUnauthorizedException(
                    errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.code),
                    detailMessage = errorResponse.msg,
                )
            HttpStatus.FORBIDDEN ->
                throw KakaoForbiddenException(
                    errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.code),
                    detailMessage = errorResponse.msg,
                )
            HttpStatus.SERVICE_UNAVAILABLE ->
                throw KakaoServiceUnavailableException(
                    errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.code),
                    detailMessage = errorResponse.msg,
                )
            else -> throw KakaoServerException(errorCode = UNKNOWN)
        }
    }
}

private data class KapiErrorResponse(
    val code: Int,
    val msg: String?,
)

internal fun <T : Any> Response.toObject(objectType: KClass<T>): T {
    try {
        return body().asInputStream().use { bodyInputStream ->
            jacksonObjectMapper().readValue(bodyInputStream, objectType.java)
        }
    } catch (e: Exception) {
        throw KakaoServerException(
            errorCode = UNKNOWN,
            detailMessage = "Kakao Api 에러 메시지 파싱에 실패했습니다. 대상 class: ${objectType.simpleName}"
        )
    }
}
