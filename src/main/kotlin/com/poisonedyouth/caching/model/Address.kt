package com.poisonedyouth.caching.model

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.poisonedyouth.caching.failure.Failure

private const val MINIMUM_VALID_ZIP_CODE = 10000
private const val MAXIMUM_VALID_ZIP_CODE = 99999

sealed interface Address {
    val id: Identity
    val streetName: String
    val streetNumber: String
    val zipCode: Int
    val city: String

    companion object {
        operator fun invoke(
            id: Identity,
            streetName: String,
            streetNumber: String,
            zipCode: Int,
            city: String
        ): Either<Failure, Address> {
            return AddressModel.create(id, streetName, streetNumber, zipCode, city)
        }
    }

    private data class AddressModel private constructor(
        override val id: Identity,
        override val streetName: String,
        override val streetNumber: String,
        override val zipCode: Int,
        override val city: String
    ) : Address {

        companion object {
            fun create(
                id: Identity,
                streetName: String,
                streetNumber: String,
                zipCode: Int,
                city: String
            ): Either<Failure, Address> {
                return either {
                    ensure(streetName.isNotEmpty()) {
                        Failure.ValidationFailure("The streetName must not be empty!")
                    }
                    ensure(streetNumber.isNotEmpty()) {
                        Failure.ValidationFailure("The streetNumber must not be empty!")
                    }
                    ensure(zipCode in MINIMUM_VALID_ZIP_CODE..MAXIMUM_VALID_ZIP_CODE) {
                        Failure.ValidationFailure("The zipCode must be between 10000 and 99999!")
                    }
                    ensure(city.isNotEmpty()) {
                        Failure.ValidationFailure("The city must not be empty.")
                    }
                    AddressModel(
                        id = id,
                        streetName = streetName,
                        streetNumber = streetNumber,
                        zipCode = zipCode,
                        city = city
                    )
                }
            }
        }
    }
}
