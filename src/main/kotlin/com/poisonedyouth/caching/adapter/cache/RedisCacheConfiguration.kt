package com.poisonedyouth.caching.adapter.cache

import com.poisonedyouth.caching.plugins.config
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

private val cacheConfig = Config().apply {
    useSingleServer().address = "redis://${config.getString("ktor.cache.redisUrl")}"
}

val redisson: RedissonClient by lazy {
    Redisson.create(cacheConfig)
}
