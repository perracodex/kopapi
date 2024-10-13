/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.dsl.elements

/**
 * Holds the license information for the API.
 *
 * @param name The name of the license.
 * @param url The URL of the license.
 *
 */
internal data class ApiLicense(
    val name: String? = null,
    val url: String? = null
)
