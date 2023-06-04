package com.poisonedyouth.caching.port

import arrow.core.Either
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.model.Identity
import com.poisonedyouth.caching.model.User

interface UserRepository {
    fun save(user: User): Either<Failure, User>
    fun findBy(userId: Identity): Either<Failure, User?>
    fun update(user: User): Either<Failure, User>
    fun delete(userId: Identity): Either<Failure, Unit>
}
