package com.whatever.caramel.domain

import com.whatever.caramel.TestApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.test.context.ActiveProfiles

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("test")
@Import(DomainIntegrationTestConfig::class)
annotation class CaramelDomainSpringBootTest

@TestConfiguration
class DomainIntegrationTestConfig {
    @Bean(name = ["textEncryptor"])
    fun testTextEncryptor(): TextEncryptor {
        return Encryptors.noOpText()
    }
}