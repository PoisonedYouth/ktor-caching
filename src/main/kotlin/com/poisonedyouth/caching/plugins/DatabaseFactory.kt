package com.poisonedyouth.caching.plugins

import com.poisonedyouth.caching.adapter.persistence.AddressTable
import com.poisonedyouth.caching.adapter.persistence.UserTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
    val databaseFactory by inject<DatabaseFactory>()
    databaseFactory.connect(environment.config)
}

interface DatabaseFactory {
    fun connect(config: ApplicationConfig)
}

class DefaultDatabaseFactory : DatabaseFactory {
    override fun connect(config: ApplicationConfig) {
        val jdbcUrl: String = config.property("ktor.storage.jdbcUrl").getString()
        val driverClassName: String = config.property("ktor.storage.driverClassName").getString()
        val user: String = config.property("ktor.storage.username").getString()
        val password: String = config.property("ktor.storage.password").getString()

        val datasource = HikariDataSource(HikariConfig().apply {
            setDriverClassName(driverClassName)
            setJdbcUrl(jdbcUrl)
            setPassword(password)
            username = user
            maximumPoolSize = 3
            minimumIdle = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        })
        val database = Database.connect(datasource)
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(AddressTable, UserTable)
        }
    }
}
