package com.whatever.domain.base

import org.springframework.data.redis.core.RedisTemplate

abstract class RedisRepository(
    protected val redisTemplate: RedisTemplate<String, String>,
)