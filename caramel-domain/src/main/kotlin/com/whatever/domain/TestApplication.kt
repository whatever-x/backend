package com.whatever.domain

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["com.whatever.domain", "com.whatever.caramel.common"]
)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
