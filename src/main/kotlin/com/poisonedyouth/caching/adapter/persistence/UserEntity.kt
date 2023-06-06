package com.poisonedyouth.caching.adapter.persistence

import arrow.core.Either
import arrow.core.raise.either
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.model.Address
import com.poisonedyouth.caching.model.UUIDIdentity
import com.poisonedyouth.caching.model.User
import java.io.Serializable
import java.time.LocalDate
import java.util.UUID

data class UserEntity(
    val id: UUID?,
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate,
    val address: AddressEntity
) : Serializable

data class AddressEntity(
    val id: UUID?,
    val streetName: String,
    val streetNumber: String,
    val zipCode: Int,
    val city: String
) : Serializable

fun User.toUserEntity() = UserEntity(
    id = this.id.getIdOrNull(),
    firstName = this.firstName,
    lastName = this.lastName,
    birthDate = this.birthDate,
    address = this.address.toAddressEntity()
)

fun Address.toAddressEntity() = AddressEntity(
    id = this.id.getIdOrNull(),
    streetName = this.streetName,
    streetNumber = this.streetNumber,
    zipCode = this.zipCode,
    city = this.city

)

fun UserEntity.toUser(): Either<Failure, User> = either {
    val address = this@toUser.address.toAddress().bind()
    return User(
        id = UUIDIdentity.fromNullableUUID(this@toUser.id),
        firstName = this@toUser.firstName,
        lastName = this@toUser.lastName,
        birthDate = this@toUser.birthDate,
        address = address
    )
}

fun AddressEntity.toAddress() = Address(
    id = UUIDIdentity.fromNullableUUID(this.id),
    streetName = this.streetName,
    streetNumber = this.streetNumber,
    zipCode = this.zipCode,
    city = this.city
)