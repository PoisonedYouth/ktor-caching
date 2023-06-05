package com.poisonedyouth.caching.model

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.poisonedyouth.caching.failure.Failure
import java.io.Serializable
import java.time.LocalDate
import java.util.UUID

private const val MINIMUM_YEAR = 1900

interface User : Serializable {
    val id: Identity
    val firstName: String
    val lastName: String
    val birthDate: LocalDate
    val address: Address

    companion object {
        operator fun invoke(
            id: Identity,
            firstName: String,
            lastName: String,
            birthDate: LocalDate,
            address: Address
        ): Either<Failure, User> {
            return UserModel.create(id, firstName, lastName, birthDate, address)
        }
    }

    private data class UserModel private constructor(
        override val id: Identity,
        override val firstName: String,
        override val lastName: String,
        override val birthDate: LocalDate,
        override val address: Address
    ) : User {

        companion object {
            fun create(
                id: Identity,
                firstName: String,
                lastName: String,
                birthDate: LocalDate,
                address: Address
            ): Either<Failure, User> {
                return either {
                    ensure(firstName.isNotEmpty()) {
                        Failure.ValidationFailure("Firstname must not be empty!")
                    }
                    ensure(lastName.isNotEmpty()) {
                        Failure.ValidationFailure("Lastname must not be empty!")
                    }
                    ensure(birthDate in LocalDate.of(MINIMUM_YEAR, 1, 1)..LocalDate.now()) {
                        Failure.ValidationFailure("Birthdate must be between 1900 and ${LocalDate.now().year}")
                    }
                    UserModel(
                        id = id,
                        firstName = firstName,
                        lastName = lastName,
                        birthDate = birthDate,
                        address = address
                    )
                }
            }
        }
    }
}

sealed interface Identity : Serializable {
    fun getIdOrNull(): UUID? {
        return when (this) {
            is UUIDIdentity -> this.id
            NoIdentity -> null
        }
    }
}

object NoIdentity : Identity

data class UUIDIdentity(val id: UUID) : Identity {
    companion object {
        fun fromNullableString(value: String?): Identity {
            if (value == null) {
                return NoIdentity
            }
            return UUIDIdentity(UUID.fromString(value))
        }
    }
}
