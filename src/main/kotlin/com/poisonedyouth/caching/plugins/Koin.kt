package com.poisonedyouth.caching.plugins

import com.poisonedyouth.caching.adapter.UserController
import com.poisonedyouth.caching.adapter.cache.ExposedCustomCacheUserRepository
import com.poisonedyouth.caching.adapter.cache.ExposedEhcacheUserRepository
import com.poisonedyouth.caching.adapter.cache.ExposedRedisCacheUserRepository
import com.poisonedyouth.caching.adapter.cache.cacheManager
import com.poisonedyouth.caching.adapter.cache.redisson
import com.poisonedyouth.caching.adapter.persistence.ExposedUserRepository
import com.poisonedyouth.caching.port.UserPort
import com.poisonedyouth.caching.port.UserRepository
import com.poisonedyouth.caching.service.UserUseCase
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val config: Config = ConfigFactory.load()

fun KoinApplication.defaultModule() = modules(defaultModule)
val defaultModule = module {
    singleOf(::UserUseCase) bind UserPort::class
    singleOf(::UserController) bind UserController::class
    singleOf(::DefaultDatabaseFactory) bind DatabaseFactory::class

    single(createdAtStart = true) {
        getCacheRepository()
    } bind UserRepository::class
}

private fun getCacheRepository(): UserRepository {
    return when (config.getString("ktor.cache.provider")) {
        "custom" -> getDefaultCacheRepository()

        "ehcache" -> ExposedEhcacheUserRepository(
            delegate = ExposedUserRepository(),
            cacheManager = cacheManager
        )

        "redis" -> ExposedRedisCacheUserRepository(
            delegate = ExposedUserRepository(),
            redisson = redisson
        )

        else -> getDefaultCacheRepository()


    }
}

private fun getDefaultCacheRepository(): UserRepository {
    return ExposedCustomCacheUserRepository(
        delegate = ExposedUserRepository(),
    )
}

fun Application.configureDependencyInjection() {
    // Install Ktor features
    install(Koin) {
        slf4jLogger()
        modules(defaultModule)
    }
}
