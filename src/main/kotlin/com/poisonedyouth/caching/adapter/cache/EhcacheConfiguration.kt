package com.poisonedyouth.caching.adapter.cache

import com.poisonedyouth.caching.adapter.persistence.UserEntity
import com.poisonedyouth.caching.plugins.config
import org.ehcache.PersistentCacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import java.io.File
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.UUID

val cacheManager: PersistentCacheManager by lazy {
    CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerBuilder.persistence(File(config.getString("ktor.cache.storagePath"))))
        .withCache(
            "userCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(
                UUID::class.java, UserEntity::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(10, MemoryUnit.MB)
                    .disk(100, MemoryUnit.MB, true)
            ).withExpiry(
                ExpiryPolicyBuilder.timeToIdleExpiration(Duration.of(100, ChronoUnit.MINUTES))
            )
        )
        .build(true)
}

