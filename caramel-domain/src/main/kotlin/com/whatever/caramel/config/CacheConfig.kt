package com.whatever.caramel.config

import com.whatever.caramel.domain.user.model.LoginPlatform
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@EnableCaching
@Configuration
class RedisCacheConfig {

    @Bean(name = ["oidcCacheManager"])
    fun oidcCacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val stringSerializationPair =
            RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
        val jsonSerializerPair =
            RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer())

        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(stringSerializationPair)
            .serializeValuesWith(jsonSerializerPair)
            .entryTtl(CacheType.OIDC_PUBLIC_KEY.ttl)

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(redisCacheConfiguration)
            .build()
    }
}

enum class CacheType(
    val cacheName: String,
    val ttl: Duration = Duration.ofDays(7L),
    val maximumSize: Long = LoginPlatform.entries.size.toLong(),
) {
    OIDC_PUBLIC_KEY("oidc"),
}
