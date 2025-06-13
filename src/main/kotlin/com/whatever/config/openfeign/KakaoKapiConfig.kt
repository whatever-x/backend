package com.whatever.config.openfeign

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.whatever.global.exception.externalserver.kakao.KakaoBadRequestException
import com.whatever.global.exception.externalserver.kakao.KakaoForbiddenException
import com.whatever.global.exception.externalserver.kakao.KakaoServerException
import com.whatever.global.exception.externalserver.kakao.KakaoServerExceptionCode
import com.whatever.global.exception.externalserver.kakao.KakaoServerExceptionCode.UNKNOWN
import com.whatever.global.exception.externalserver.kakao.KakaoServiceUnavailableException
import com.whatever.global.exception.externalserver.kakao.KakaoUnauthorizedException
import feign.Response
import feign.codec.Encoder
import feign.codec.ErrorDecoder
import feign.form.FormEncoder
import feign.form.spring.SpringFormEncoder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {  }

class KakaoKapiConfig {

    @Bean
    fun formEncoder(converters: ObjectFactory<HttpMessageConverters>): Encoder {
        return SpringFormEncoder(SpringEncoder(converters))
    }

    @Bean
    fun kapiErrorDecoder(): KapiErrorDecoder {
        return KapiErrorDecoder()
    }
}

class KapiErrorDecoder() : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        if (response.status() < 400) {
            return ErrorDecoder.Default().decode(methodKey, response)
        }

        val errorResponse = response.toObject(KapiErrorResponse::class)
        logger.error { "kapi exception. response: ${errorResponse}" }

        when (HttpStatus.valueOf(response.status())) {
            HttpStatus.BAD_REQUEST ->
                throw KakaoBadRequestException(
                    errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.code),
                )
            HttpStatus.UNAUTHORIZED ->
                throw KakaoUnauthorizedException(
                    errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.code),
                )
            HttpStatus.FORBIDDEN ->
                throw KakaoForbiddenException(
                    errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.code),
                )
            HttpStatus.SERVICE_UNAVAILABLE ->
                throw KakaoServiceUnavailableException(
                    errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.code),
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
        logger.warn { "Kakao Api 에러 메시지 파싱에 실패했습니다. 대상 class: ${objectType.simpleName}" }
        throw KakaoServerException(errorCode = UNKNOWN)
    }
}
