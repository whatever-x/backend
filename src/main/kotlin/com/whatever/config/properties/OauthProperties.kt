package com.whatever.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("oauth")
data class OauthProperties (
    val kakao: OauthSecret
)

data class OauthSecret(
    val baseUrl: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUrl: String,
    val appId: String,
    val adminKey: String,
)
