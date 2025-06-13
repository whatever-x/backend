package com.whatever.config.openfeign

import com.whatever.global.exception.externalserver.kakao.KakaoServerException
import com.whatever.global.exception.externalserver.kakao.KakaoServerExceptionCode
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

private val logger = KotlinLogging.logger {  }

class KakaoKauthConfig {

    @Bean
    fun formEncoder(converters: ObjectFactory<HttpMessageConverters>): Encoder {
        return SpringFormEncoder(SpringEncoder(converters))
    }

    @Bean
    fun kauthErrorDecoder(): KauthErrorDecoder {
        return KauthErrorDecoder()
    }
}

class KauthErrorDecoder() : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        if (response.status() < 400) {
            return ErrorDecoder.Default().decode(methodKey, response)
        }

        val errorResponse = response.toObject(KauthErrorResponse::class)
        logger.error { "kauth exception. response: ${errorResponse}" }

        throw KakaoServerException(
            errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.error),
        )
    }
}

private data class KauthErrorResponse(
    val error: String?,  // 에러 코드
    val errorDescription: String?
)