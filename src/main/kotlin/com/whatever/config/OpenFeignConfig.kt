package com.whatever.config

import feign.FeignException
import feign.Logger
import feign.Response
import feign.codec.Encoder
import feign.codec.ErrorDecoder
import feign.form.FormEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignClientGlobalConfig {

    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }

}

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


class KakaoKauthConfig {

    @Bean
    fun formEncoder(): Encoder {
        return FormEncoder()
    }

}