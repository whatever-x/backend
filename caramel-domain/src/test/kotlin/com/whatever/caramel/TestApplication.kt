package com.whatever.caramel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.whatever.caramel"])
@ConfigurationPropertiesScan(basePackages = ["com.whatever.caramel"])
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
