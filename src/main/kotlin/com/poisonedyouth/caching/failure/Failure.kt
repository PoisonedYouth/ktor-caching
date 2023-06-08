package com.poisonedyouth.caching.failure

import arrow.core.Either
import org.slf4j.Logger

sealed interface Failure {
    val message: String

    data class ValidationFailure(override val message: String) : Failure

    data class GenericFailure(val e: Throwable) : Failure {
        override val message: String = e.localizedMessage
    }
}

@Suppress("TooGenericExceptionCaught") // It's intended to catch all exceptions in this function.
fun <T> eval(logger: Logger, exec: () -> T): Either<Failure, T> {
    return Either.catch {
        exec()
    }.mapLeft {
        logger.error("Failed to execute operation because of - ${it.message}")
        Failure.GenericFailure(it)
    }
}
