package com.whatever.caramel.domain

import com.whatever.caramel.common.global.jwt.JwtProperties
import com.whatever.caramel.infrastructure.properties.FirebaseProperties
import com.whatever.caramel.infrastructure.properties.OauthProperties
import com.whatever.caramel.infrastructure.properties.SpecialDayApiProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["com.whatever.caramel"]
)
@EnableConfigurationProperties(
    JwtProperties::class, OauthProperties::class, FirebaseProperties::class,
    SpecialDayApiProperties::class
)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
