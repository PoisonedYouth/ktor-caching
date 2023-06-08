package com.poisonedyouth.caching.plugins

import com.poisonedyouth.caching.adapter.cache.cacheManager
import com.poisonedyouth.caching.adapter.cache.redisson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped

fun Application.configureShutdownHooks() {
    environment.monitor.subscribe(ApplicationStopped) {
        cacheManager.close()
        redisson.shutdown()
    }
}