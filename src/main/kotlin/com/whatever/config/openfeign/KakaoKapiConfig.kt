package com.whatever.config.openfeign

import feign.FeignException
import feign.Response
import feign.codec.Encoder
import feign.codec.ErrorDecoder
import feign.form.FormEncoder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus

class KakaoKapiConfig {

    @Bean
    fun formEncoder(): Encoder {
        return FormEncoder()
    }

    @Bean
    fun formDecoder(): KapiResponseDecoder {
        return KapiResponseDecoder()
    }
}

class KapiResponseDecoder() : ErrorDecoder {

    override fun decode(methodKey: String?, response: Response?): Exception {
        if (response == null || response.status() < 400) {
            return FeignException.errorStatus(methodKey, response)
        }

        // TODO(준용) 예외 처리 추가
        when (HttpStatus.valueOf(response.status())) {
            HttpStatus.BAD_REQUEST ->
                throw IllegalArgumentException()
            HttpStatus.UNAUTHORIZED ->
                throw IllegalArgumentException()
            HttpStatus.FORBIDDEN ->
                throw IllegalArgumentException()
            HttpStatus.SERVICE_UNAVAILABLE ->
                throw IllegalArgumentException()
            else ->
                throw IllegalArgumentException()
        }
        throw RuntimeException("kakao oauth exception")

    }

}
