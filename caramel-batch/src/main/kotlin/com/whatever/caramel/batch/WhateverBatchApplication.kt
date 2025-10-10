package com.whatever.caramel.batch

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import kotlin.system.exitProcess

@ConfigurationPropertiesScan(basePackages = ["com.whatever.caramel"])
@SpringBootApplication(scanBasePackages = ["com.whatever.caramel"])
@EnableJpaRepositories(basePackages = ["com.whatever.caramel.domain"])
@EntityScan(basePackages = ["com.whatever.caramel.domain"])
class WhateverBatchApplication

fun main(args: Array<String>) {
    exitProcess(
        SpringApplication.exit(
            runApplication<WhateverBatchApplication>(*args)
        )
    )
}
