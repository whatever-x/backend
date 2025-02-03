package com.whatever.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.whatever.domain.user.model.LoginPlatform
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@ConditionalOnProperty(
    name = ["spring.cache.type"],
    havingValue = "caffeine",
    matchIfMissing = false
)
@EnableCaching
@Configuration
class CaffeineCacheConfig {

    private val logger = KotlinLogging.logger {  }

    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> {
        return Caffeine.newBuilder()
            .expireAfterWrite(CacheType.OIDC_PUBLIC_KEY.ttl)
            .maximumSize(CacheType.OIDC_PUBLIC_KEY.maximumSize)
            .recordStats()
            .removalListener { key, value, cause -> logger.info { "Kakao OIDC Public Key가 제거되었습니다. cause: ${cause}" } }
    }

    @Bean("oidcCacheManager")
    fun oidcCacheManager(caffeine: Caffeine<Any, Any>): CacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.setCaffeine(caffeine)
        return cacheManager
    }

}

enum class CacheType(
    val cacheName: String,
    val ttl: Duration = Duration.ofDays(7L),
    val maximumSize: Long = LoginPlatform.entries.size.toLong(),
) {
    OIDC_PUBLIC_KEY("oidc"),
}