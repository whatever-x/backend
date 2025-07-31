package com.whatever

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = ["com.whatever"])
class WhateverBackendApplication

fun main(args: Array<String>) {
    runApplication<WhateverBackendApplication>(*args)
}
