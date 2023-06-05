package com.poisonedyouth.caching.plugins

import com.poisonedyouth.caching.adapter.persistence.AddressTable
import com.poisonedyouth.caching.adapter.persistence.UserTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
    val databaseFactory by inject<DatabaseFactory>()
    databaseFactory.connect()
}

interface DatabaseFactory {
    fun connect()
}

class DefaultDatabaseFactory : DatabaseFactory {
    private val driverClassName = "org.postgresql.Driver"
    private val jdbcURL = "jdbc:postgresql://localhost:5432/db"
    private val user = "root"
    private val password = "password"

    override fun connect() {
        val database = Database.connect(jdbcURL, driverClassName, user, password)
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(AddressTable, UserTable)
        }
    }
}
