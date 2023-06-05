package com.poisonedyouth.caching.plugins

import com.poisonedyouth.caching.adapter.persistence.ExposedUserRepository
import com.poisonedyouth.caching.adapter.UserController
import com.poisonedyouth.caching.port.UserPort
import com.poisonedyouth.caching.port.UserRepository
import com.poisonedyouth.caching.service.UserUseCase
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun KoinApplication.defaultModule() = modules(defaultModule)
val defaultModule = module {
    singleOf(::UserUseCase) bind UserPort::class
    singleOf(::UserController) bind UserController::class
    singleOf(::ExposedUserRepository) bind UserRepository::class
    singleOf(::DefaultDatabaseFactory) bind DatabaseFactory::class
}

fun Application.configureDependencyInjection() {
    // Install Ktor features
    install(Koin) {
        slf4jLogger()
        modules(defaultModule)
    }
}
