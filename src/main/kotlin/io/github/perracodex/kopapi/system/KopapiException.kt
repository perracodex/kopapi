/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.system

/**
 * Custom exception for errors that occur in the Kopapi library.
 *
 * @param message The detail message describing the validation failure.
 * @param cause Optional underlying reason for this [KopapiException].
 */
public class KopapiException @PublishedApi internal constructor(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
