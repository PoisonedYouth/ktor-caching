package com.poisonedyouth.caching.adapter.cache

import arrow.core.Either
import arrow.core.raise.either
import com.poisonedyouth.caching.adapter.persistence.UserEntity
import com.poisonedyouth.caching.adapter.persistence.toUser
import com.poisonedyouth.caching.adapter.persistence.toUserEntity
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.model.Identity
import com.poisonedyouth.caching.model.User
import com.poisonedyouth.caching.port.UserRepository
import org.redisson.api.RedissonClient
import java.util.UUID


class ExposedRedisCacheUserRepository(
    private val delegate: UserRepository,
    redisson: RedissonClient
) : UserRepository {
    private val userCache = redisson.getMapCache<UUID, UserEntity>("userCache")

    override suspend fun save(user: User): Either<Failure, User> {
        return delegate.save(user).also { userCache[user.id.getIdOrNull()] = user.toUserEntity() }
    }

    override suspend fun findBy(userId: Identity): Either<Failure, User?> = either {
        getUserFromCache(userId)
            ?: delegate.findBy(userId).bind().also { userCache[userId.getIdOrNull()] = it?.toUserEntity() }
    }

    private fun getUserFromCache(userId: Identity) = userCache[userId.getIdOrNull()]?.toUser()?.getOrNull()

    override suspend fun update(user: User): Either<Failure, User> {
        return delegate.update(user).also { userCache[user.id.getIdOrNull()] = user.toUserEntity() }
    }

    override suspend fun delete(userId: Identity): Either<Failure, Unit> {
        userCache.remove(userId.getIdOrNull())
        return delegate.delete(userId)
    }
}