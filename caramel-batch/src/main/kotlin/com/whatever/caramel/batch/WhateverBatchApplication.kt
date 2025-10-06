package com.whatever.caramel.batch

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = ["com.whatever.caramel.batch"])
class WhateverBatchApplication

fun main(args: Array<String>) {
    exitProcess(
        SpringApplication.exit(
            runApplication<WhateverBatchApplication>(*args)
        )
    )
}
