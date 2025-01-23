package com.whatever

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class WhateverBackendApplication

fun main(args: Array<String>) {
    runApplication<WhateverBackendApplication>(*args)
    val a = 1
}
