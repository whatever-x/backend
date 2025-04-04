package com.whatever.config.openfeign

import feign.codec.Encoder
import feign.form.FormEncoder
import org.springframework.context.annotation.Bean

class KakaoKauthConfig {

    @Bean
    fun formEncoder(): Encoder {
        return FormEncoder()
    }

}
