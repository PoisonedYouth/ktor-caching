package com.poisonedyouth.caching

import com.poisonedyouth.caching.plugins.configureDatabase
import com.poisonedyouth.caching.plugins.configureDependencyInjection
import com.poisonedyouth.caching.plugins.configureRouting
import com.poisonedyouth.caching.plugins.configureSerialization
import com.poisonedyouth.caching.plugins.configureShutdownHooks
import io.ktor.server.application.Application

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureDependencyInjection()
    configureDatabase()
    configureSerialization()
    configureRouting()

    configureShutdownHooks()
}
