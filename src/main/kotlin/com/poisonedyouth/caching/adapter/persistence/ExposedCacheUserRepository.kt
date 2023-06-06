package com.poisonedyouth.caching.adapter.persistence

import arrow.core.Either
import arrow.core.raise.either
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.model.Identity
import com.poisonedyouth.caching.model.User
import com.poisonedyouth.caching.port.UserRepository
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.EntryUnit
import org.ehcache.config.units.MemoryUnit
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration
import java.io.File
import java.util.UUID

class ExposedCacheUserRepository(
    private val delegate: UserRepository,
    storagePath: File
) : UserRepository {
    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerPersistenceConfiguration(storagePath))
        .withCache(
            "userCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                UUID::class.javaObjectType,
                UserEntity::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1000, EntryUnit.ENTRIES)
                    .offheap(10, MemoryUnit.MB)
                    .disk(100, MemoryUnit.MB, true)
            )
        )
        .build(true)

    private val userCache = cacheManager.getCache("userCache", UUID::class.javaObjectType, UserEntity::class.java)

    override suspend fun save(user: User): Either<Failure, User> {
        return delegate.save(user).also { userCache.put(user.id.getIdOrNull(), user.toUserEntity()) }
    }

    override suspend fun findBy(userId: Identity): Either<Failure, User?> = either {
        getUserFromCache(userId)
            ?: delegate.findBy(userId).bind().also { userCache.put(userId.getIdOrNull(), it?.toUserEntity()) }
    }

    private fun getUserFromCache(userId: Identity) = userCache.get(userId.getIdOrNull())?.toUser()?.getOrNull()

    override suspend fun update(user: User): Either<Failure, User> {
        return delegate.update(user).also { userCache.put(user.id.getIdOrNull(), user.toUserEntity()) }
    }

    override suspend fun delete(userId: Identity): Either<Failure, Unit> {
        userCache.remove(userId.getIdOrNull())
        return delegate.delete(userId)
    }
}