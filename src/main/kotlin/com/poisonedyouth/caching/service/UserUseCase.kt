package com.poisonedyouth.caching.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.model.Address
import com.poisonedyouth.caching.model.Identity
import com.poisonedyouth.caching.model.UUIDIdentity
import com.poisonedyouth.caching.model.User
import com.poisonedyouth.caching.port.UserPort
import com.poisonedyouth.caching.port.UserRepository

class UserUseCase(
    private val userRepository: UserRepository
) : UserPort {
    override suspend fun addNewUser(user: UserDto): Either<Failure, User> = either {
        userRepository.save(user.toUser().bind()).bind()
    }

    override suspend fun updateUser(user: UserDto): Either<Failure, User> = either {
        userRepository.update(user.toUser().bind()).bind()
    }

    override suspend fun deleteUser(userId: Identity): Either<Failure, Unit> {
        return userRepository.delete(userId)
    }

    override suspend fun findUser(userId: Identity): Either<Failure, User> = either {
        val existingUser = userRepository.findBy(userId).bind()
        ensureNotNull(existingUser){
            Failure.ValidationFailure("User with id ${userId.getIdOrNull()} does not exists.")
        }
        existingUser
    }
}

fun UserDto.toUser(): Either<Failure, User> = either {
    val address = this@toUser.address.toAddress().bind()
    return User(
        id = UUIDIdentity.fromNullableString(this@toUser.id),
        firstName = this@toUser.firstName,
        lastName = this@toUser.lastName,
        birthDate = this@toUser.birthDate,
        address = address
    )
}

private fun AddressDto.toAddress() = Address(
    id = UUIDIdentity.fromNullableString(this.id),
    streetName = this.streetName,
    streetNumber = this.streetNumber,
    zipCode = this.zipCode,
    city = this.city
)
