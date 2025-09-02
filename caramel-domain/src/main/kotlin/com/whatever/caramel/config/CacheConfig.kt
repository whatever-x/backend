package com.whatever.caramel.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Any::class.java).build()
        val customObjectMapper = jacksonObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING)


        val keySerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
        val valueSerializationPair =
            RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer(customObjectMapper))
        val baseConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(keySerializationPair)
            .serializeValuesWith(valueSerializationPair)

        val cacheConfigurations = CacheType.entries.associate { cacheType ->
            cacheType.cacheName to baseConfig.entryTtl(cacheType.ttl)
        }

        return RedisCacheManager.builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}

enum class CacheType(
    val cacheName: String,
    val ttl: Duration = Duration.ofDays(7L),
) {
    OIDC_PUBLIC_KEY("auth:oidc-public-key"),
    CLIENT_VERSIONS("app:client-versions"),
}
