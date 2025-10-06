package com.whatever.caramel.batch

import com.whatever.caramel.batch.config.CoupleConfig
import com.whatever.caramel.batch.config.FirebaseConfig
import com.whatever.caramel.batch.config.NotificationConfig
import com.whatever.caramel.batch.config.UserConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import kotlin.system.exitProcess

@EnableJpaAuditing
@SpringBootApplication(
    scanBasePackages = [
        "com.whatever.caramel.batch",
        "com.whatever.caramel.domain.notification",
        "com.whatever.caramel.domain.couple",
        "com.whatever.caramel.domain.firebase",
        "com.whatever.caramel.infrastructure",
    ]
)
@Import(
    CoupleConfig::class,
    FirebaseConfig::class,
    NotificationConfig::class,
    UserConfig::class,
)
class WhateverBatchApplication

fun main(args: Array<String>) {
    exitProcess(
        SpringApplication.exit(
            runApplication<WhateverBatchApplication>(*args)
        )
    )
}
