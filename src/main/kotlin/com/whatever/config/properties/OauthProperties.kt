package com.whatever.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("oauth")
data class OauthProperties (
    val kakao: KakaoOauthSecret,
    val apple: AppleOauthSecret,
)

data class KakaoOauthSecret(
    val baseUrl: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUrl: String,
    val appId: String,
    val adminKey: String,
) {
    val adminKeyWithPrefix
        get() = "KakaoAK ${adminKey}"
}

data class AppleOauthSecret(
    val baseUrl: String,
    val teamId: String,
    val serviceId: String,
    val keyId: String,
    val keyPath: String,
    val redirectUrl: String,
)
