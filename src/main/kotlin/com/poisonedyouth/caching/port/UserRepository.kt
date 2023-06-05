package com.poisonedyouth.caching.port

import arrow.core.Either
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.model.Identity
import com.poisonedyouth.caching.model.User

interface UserRepository {
    suspend fun save(user: User): Either<Failure, User>
    suspend fun findBy(userId: Identity): Either<Failure, User?>
    suspend fun update(user: User): Either<Failure, User>
    suspend fun delete(userId: Identity): Either<Failure, Unit>
}
