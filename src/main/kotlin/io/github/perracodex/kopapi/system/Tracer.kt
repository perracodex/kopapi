/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.system

import io.github.perracodex.kopapi.util.safeName
import io.ktor.util.logging.*
import org.slf4j.Logger
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * A simple tracer wrapper to provide a consistent logging interface.
 */
internal class Tracer(private val logger: Logger) {
    /**
     * Logs a message with debug severity level.
     */
    fun debug(message: String) {
        if (enabled) {
            logger.debug(message)
        }
    }

    /**
     * Logs a message with info severity level.
     */
    fun info(message: String) {
        if (enabled) {
            logger.info(message)
        }
    }

    /**
     * Logs a message with warning severity level.
     */
    fun warning(message: String) {
        if (enabled) {
            logger.warn(message)
        }
    }

    /**
     * Logs a message with error severity level.
     */
    fun error(message: String) {
        if (enabled) {
            logger.error(message)
        }
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

    /**
     * Logs a message with the specified severity level.
     *
     * #### Usage
     *
     * - Class-based logging:
     * ```
     * class SomeClass {
     *      private val tracer = Tracer<SomeClass>()
     *
     *      fun someFunction() {
     *          tracer.info("Logging message.")
     *      }
     * }
     * ```
     *
     * - Top-level and extension functions:
     * ```
     * Tracer(ref = ::someTopLevelFunction).info("Logging message.")
     * ```
     */
    companion object {
        /** Toggle for full package name or simple name. */
        const val LOG_FULL_PACKAGE: Boolean = true

        /** Default group name for package filtering. */
        private const val GROUP: String = "io.github.perracodex.kopapi."

        /** Enables or disables the Tracer. */
        var enabled: Boolean = true

        /** Logger instance for disabled Tracer. */
        private val disabledLogger: Tracer = Tracer(logger = KtorSimpleLogger(name = "Disabled"))

        /**
         * Creates a new [Tracer] instance for a given class.
         * Intended for classes where the class context is applicable.
         *
         * #### Usage
         * ```
         * class SomeClass {
         *      private val tracer = Tracer<SomeClass>()
         *
         *      fun someFunction() {
         *          tracer.info("Logging message.")
         *      }
         * }
         * ```
         *
         * @param T The class for which the logger is being created.
         * @return Tracer instance with a logger named after the class.
         */
        inline operator fun <reified T : Any> invoke(): Tracer {
            if (!enabled) {
                return disabledLogger
            }

            val loggerName: String = when {
                LOG_FULL_PACKAGE ->
                    T::class.qualifiedName?.removePrefix(prefix = GROUP)
                        ?: T::class.safeName()

                else ->
                    T::class.safeName()
            }
            return Tracer(logger = KtorSimpleLogger(name = loggerName))
        }

        /**
         * Creates a new [Tracer] instance intended for top-level and extension functions
         * where class context is not applicable.
         *
         * #### Usage
         * ```
         * Tracer(ref = ::someTopLevelFunction).info("Logging message.")
         * ```
         *
         * @param ref The source reference to the top-level or extension function.
         * @return Tracer instance named after the function and its declaring class (if available).
         */
        operator fun <T> invoke(ref: KFunction<T>): Tracer {
            if (!enabled) {
                return disabledLogger
            }

            val loggerName: String = when {
                LOG_FULL_PACKAGE ->
                    "${
                        ref.javaMethod?.declaringClass?.name?.removePrefix(prefix = GROUP)
                            ?: "Unknown"
                    }.${ref.name}"

                else ->
                    ref.name
            }
            return Tracer(logger = KtorSimpleLogger(name = loggerName))
        }
    }
}
