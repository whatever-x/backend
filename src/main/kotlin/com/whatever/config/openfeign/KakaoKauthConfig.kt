package com.whatever.config.openfeign

import com.whatever.global.exception.externalserver.kakao.KakaoServerException
import com.whatever.global.exception.externalserver.kakao.KakaoServerExceptionCode
import feign.FeignException
import feign.Response
import feign.codec.Encoder
import feign.codec.ErrorDecoder
import feign.form.FormEncoder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean

private val logger = KotlinLogging.logger {  }

class KakaoKauthConfig {

    @Bean
    fun formEncoder(): Encoder {
        return FormEncoder()
    }

    @Bean
    fun kauthErrorDecoder(): KauthErrorDecoder {
        return KauthErrorDecoder()
    }
}

class KauthErrorDecoder() : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response?): Exception {
        if (response == null || response.status() < 400) {
            return FeignException.errorStatus(methodKey, response)
        }

        val errorResponse = response.toObject(KauthErrorResponse::class)
        logger.error { "kauth exception. response: ${errorResponse}" }

        throw KakaoServerException(
            errorCode = KakaoServerExceptionCode.fromKakaoErrorCode(errorResponse.error),
            detailMessage = errorResponse.errorDescription,
        )
    }
}

private data class KauthErrorResponse(
    val error: String?,  // 에러 코드
    val errorDescription: String?
)