package com.poisonedyouth.caching

import com.poisonedyouth.caching.plugins.configureDependencyInjection
import com.poisonedyouth.caching.plugins.configureRouting
import com.poisonedyouth.caching.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDependencyInjection()
    configureSerialization()
    configureRouting()
}
