package com.poisonedyouth.caching.adapter.persistence

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.failure.eval
import com.poisonedyouth.caching.model.Address
import com.poisonedyouth.caching.model.Identity
import com.poisonedyouth.caching.model.UUIDIdentity
import com.poisonedyouth.caching.model.User
import com.poisonedyouth.caching.port.UserRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

open class ExposedUserRepository : UserRepository {
    private val logger = LoggerFactory.getLogger(ExposedUserRepository::class.java)

    override suspend fun save(user: User): Either<Failure, User> = dbQuery {
        either {
            val existingUser = findBy(user.id).bind()
            ensure(existingUser == null) {
                Failure.ValidationFailure("User with id '${user.id.getIdOrNull()}' already exists.")
            }
            eval(logger) {
                val id = UserTable.insertAndGetId { insertUserStatement ->
                    insertUserStatement[firstName] = user.firstName
                    insertUserStatement[lastName] = user.lastName
                    insertUserStatement[birthDate] = user.birthDate
                    insertUserStatement[address] = AddressTable.insertAndGetId { insertAddressStatement ->
                        insertAddressStatement[streetName] = user.address.streetName
                        insertAddressStatement[streetNumber] = user.address.streetNumber
                        insertAddressStatement[zipCode] = user.address.zipCode
                        insertAddressStatement[city] = user.address.city
                    }
                }.value
                User(
                    id = UUIDIdentity(id),
                    firstName = user.firstName,
                    lastName = user.lastName,
                    birthDate = user.birthDate,
                    address = user.address
                ).bind()
            }.bind()
        }
    }

    override suspend fun findBy(userId: Identity): Either<Failure, User?> = dbQuery {
        eval(logger) {
            val existingUser = UserTable.select { UserTable.id eq userId.getIdOrNull() }.firstOrNull()
            existingUser?.let { resultRow ->
                mapResultRowToUser(resultRow).getOrNull()
            }
        }
    }

    override suspend fun update(user: User): Either<Failure, User> = dbQuery {
        either {
            val existingUser = findBy(user.id).bind()
            ensure(existingUser != null) {
                Failure.ValidationFailure("User with id '${user.id.getIdOrNull()}' does not exists.")
            }
            eval(logger) {
                UserTable.update({ UserTable.id eq user.id.getIdOrNull() }) { userUpdateStatement ->
                    userUpdateStatement[firstName] = user.firstName
                    userUpdateStatement[lastName] = user.lastName
                    userUpdateStatement[birthDate] = user.birthDate
                }
                AddressTable.update({ AddressTable.id eq user.address.id.getIdOrNull() }) { addressUpdateStatement ->
                    addressUpdateStatement[streetName] = user.address.streetName
                    addressUpdateStatement[streetNumber] = user.address.streetNumber
                    addressUpdateStatement[zipCode] = user.address.zipCode
                    addressUpdateStatement[city] = user.address.city
                }
                user
            }.bind()
        }
    }

    override suspend fun delete(userId: Identity): Either<Failure, Unit> = dbQuery {
        either {
            UserTable.deleteWhere { UserTable.id eq userId.getIdOrNull() }
            Unit
        }
    }

    private fun mapResultRowToUser(userResultRow: ResultRow): Either<Failure, User> =
        mapResultRowToAddress(userResultRow).flatMap {
            User(
                id = UUIDIdentity(userResultRow[UserTable.id].value),
                firstName = userResultRow[UserTable.firstName],
                lastName = userResultRow[UserTable.lastName],
                birthDate = userResultRow[UserTable.birthDate],
                address = it
            )
        }

    private fun mapResultRowToAddress(userResultRow: ResultRow): Either<Failure, Address> {
        return AddressTable.select { AddressTable.id eq userResultRow[UserTable.address] }.first().let {
            Address(
                id = UUIDIdentity(it[AddressTable.id].value),
                streetName = it[AddressTable.streetName],
                streetNumber = it[AddressTable.streetNumber],
                zipCode = it[AddressTable.zipCode],
                city = it[AddressTable.city]
            )
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

}