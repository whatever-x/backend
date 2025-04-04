package com.whatever.config.openfeign

import feign.FeignException
import feign.Response
import feign.codec.Encoder
import feign.codec.ErrorDecoder
import feign.form.FormEncoder
import org.springframework.context.annotation.Bean

class KakaoOAuthConfig {

    @Bean
    fun formEncoder(): Encoder {
        return FormEncoder()
    }

    @Bean
    fun formDecoder(): KakaoResponseDecoder {
        return KakaoResponseDecoder()
    }
}

class KakaoResponseDecoder() : ErrorDecoder {

    override fun decode(methodKey: String?, response: Response?): Exception {
        if (response == null || response.status() < 400) {
            return FeignException.errorStatus(methodKey, response)
        }

        // TODO(준용) 예외 처리 추가
//        when (HttpStatus.valueOf(response.status())) {
//        }
        throw RuntimeException("kakao oauth exception")

    }

}
