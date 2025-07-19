package com.whatever.caramel.infrastructure.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperties(
    val host: String,
    val port: String,
    val password: String,
) {

    val postAsInt: Int
        get() = port.toInt()
}
