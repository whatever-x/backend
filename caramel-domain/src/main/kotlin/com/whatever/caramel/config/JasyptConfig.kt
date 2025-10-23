package com.whatever.caramel.config

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMConfig
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMStringEncryptor
import org.jasypt.encryption.StringEncryptor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableEncryptableProperties
class JasyptConfig {

    @Bean("jasyptContentEncryptor")
    fun stringEncryptor(jasyptProperties: JasyptProperties): StringEncryptor {
        val config = SimpleGCMConfig().apply {
            secretKey = jasyptProperties.gcmSecretKeyString
        }
        return SimpleGCMStringEncryptor(config)
    }
}

@ConfigurationProperties(prefix = "jasypt.encryptor")
data class JasyptProperties(
    val gcmSecretKeyString: String,
)