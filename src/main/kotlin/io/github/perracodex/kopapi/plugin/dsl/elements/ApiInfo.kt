/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.dsl.elements

/**
 * Builder for the `Info` section of the OpenAPI schema.
 *
 * @property title The title of the API.
 * @property description The description of the API.
 * @property version The version of the API.
 * @property termsOfService The terms of service for the API.
 * @property contact The contact information for the API.
 * @property license The license information for the API.
 */
internal data class ApiInfo(
    val title: String,
    val description: String,
    val version: String,
    val termsOfService: String?,
    val contact: ApiContact?,
    val license: ApiLicense?
)
