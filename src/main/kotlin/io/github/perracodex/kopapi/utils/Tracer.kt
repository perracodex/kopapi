/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.utils

import io.ktor.util.logging.*
import org.slf4j.Logger
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * A simple tracer wrapper to provide a consistent logging interface.
 */
@Suppress("unused")
internal class Tracer(private val logger: Logger) {
    /**
     * Logs a message with debug severity level.
     */
    fun debug(message: String) {
        logger.debug(message)
    }

    /**
     * Logs a message with info severity level.
     */
    fun info(message: String) {
        logger.info(message)
    }

    /**
     * Logs a message with warning severity level.
     */
    fun warning(message: String) {
        logger.warn(message)
    }

    /**
     * Logs a message with error severity level.
     */
    fun error(message: String) {
        logger.error(message)
    }

    /**
     * Logs a message with error severity level and an associated [Throwable].
     *
     * @param message The message to log.
     * @param cause The [Throwable] associated with the error.
     */
    fun error(message: String? = "Unexpected Exception", cause: Throwable) {
        logger.error(message, cause)
    }

    companion object {
        /** Toggle for full package name or simple name. */
        const val LOG_FULL_PACKAGE: Boolean = true

        /**
         * Creates a new [Tracer] instance for a given class.
         * Intended for classes where the class context is applicable.
         *
         * @param T The class for which the logger is being created.
         * @return Tracer instance with a logger named after the class.
         */
        inline operator fun <reified T : Any> invoke(): Tracer {
            val loggerName: String = if (LOG_FULL_PACKAGE) {
                T::class.qualifiedName ?: T::class.simpleName ?: "UnknownClass"
            } else {
                T::class.simpleName ?: "UnknownClass"
            }
            return Tracer(logger = KtorSimpleLogger(name = loggerName))
        }

        /**
         * Creates a new [Tracer] instance intended for top-level and extension functions
         * where class context is not applicable.
         *
         * @param ref The source reference to the top-level or extension function.
         * @return Tracer instance named after the function and its declaring class (if available).
         */
        operator fun <T> invoke(ref: KFunction<T>): Tracer {
            val loggerName = if (LOG_FULL_PACKAGE) {
                "${ref.javaMethod?.declaringClass?.name ?: "Unknown"}.${ref.name}"
            } else {
                ref.name
            }
            return Tracer(logger = KtorSimpleLogger(name = loggerName))
        }
    }
}
