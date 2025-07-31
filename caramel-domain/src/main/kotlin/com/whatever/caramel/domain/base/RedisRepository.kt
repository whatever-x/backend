package com.whatever.caramel.domain.base

import org.springframework.data.redis.core.RedisTemplate

abstract class RedisRepository(
    protected val redisTemplate: RedisTemplate<String, String>,
) {
    protected fun getRemainingTtlSeconds(key: String): Long {
        val remainingSeconds = redisTemplate.getExpire(key)
        if (remainingSeconds < 0) {
            return 0
        }
        return remainingSeconds
    }
}
