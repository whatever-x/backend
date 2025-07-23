package com.whatever.caramel.infrastructure.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kor.openapi.specialday")
data class SpecialDayApiProperties(
    val key: String,
)
