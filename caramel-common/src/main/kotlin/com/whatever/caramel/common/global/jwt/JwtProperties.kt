package com.whatever.caramel.common.global.jwt

import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.crypto.SecretKey

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    private val secretKeyStr: String,
    val accessExpirationSec: Long,
    val refreshExpirationSec: Long,
    val issuer: String,
) {
    val secretKey: SecretKey = Keys.hmacShaKeyFor(secretKeyStr.toByteArray())
}
