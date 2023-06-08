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
import org.ehcache.PersistentCacheManager
import java.util.*

class ExposedEhcacheUserRepository(
    private val delegate: UserRepository,
    cacheManager: PersistentCacheManager,
) : UserRepository {

    private val userCache =
        cacheManager.getCache("userCache", UUID::class.java, UserEntity::class.java)

    override suspend fun save(user: User): Either<Failure, User> = either {
        delegate.save(user).bind().also { putUserToCache(it) }
    }

    override suspend fun findBy(userId: Identity): Either<Failure, User?> = either {
        getUserFromCache(userId)
            ?: delegate.findBy(userId).bind().also { putUserToCache(it) }
    }

    override suspend fun update(user: User): Either<Failure, User> = either {
        delegate.update(user).bind().also { putUserToCache(it) }
    }

    override suspend fun delete(userId: Identity): Either<Failure, Unit> {
        userCache.remove(userId.getIdOrNull())
        return delegate.delete(userId)
    }

    private fun getUserFromCache(userId: Identity) = userCache.get(userId.getIdOrNull())?.toUser()?.getOrNull()

    private fun putUserToCache(user: User?) = either {
        if (user != null) {
            userCache.put(user.id.getIdOrFailure().bind(), user.toUserEntity())
        }
    }
}