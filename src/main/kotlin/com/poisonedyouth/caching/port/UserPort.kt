package com.poisonedyouth.caching.port

import arrow.core.Either
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.model.Identity
import com.poisonedyouth.caching.model.User
import com.poisonedyouth.caching.service.UserDto

interface UserPort {
    fun addNewUser(user: UserDto): Either<Failure, User>
    fun updateUser(user: UserDto): Either<Failure, User>
    fun deleteUser(userId: Identity): Either<Failure, Unit>
    fun findUser(userId: Identity): Either<Failure, User>
}
