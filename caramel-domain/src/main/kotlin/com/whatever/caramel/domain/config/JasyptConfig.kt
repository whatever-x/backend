package com.whatever.caramel.domain.config

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties
import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor
import org.jasypt.iv.RandomIvGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableEncryptableProperties
class JasyptConfig(
    @Value("\${jasypt.encryptor.password}")
    private val encryptorPassword: String,
    @Value("\${jasypt.encryptor.algorithm}")
    private val algorithm: String,
    @Value("\${jasypt.encryptor.pool-size}")
    private val poolSize: Int,
) {

    @Bean("jasyptStringEncryptor")
    fun stringEncryptor(): StringEncryptor {
        return PooledPBEStringEncryptor().apply {
            setPassword(encryptorPassword)
            setAlgorithm(algorithm)
            setIvGenerator(RandomIvGenerator())
            setPoolSize(poolSize)
        }
    }
}

