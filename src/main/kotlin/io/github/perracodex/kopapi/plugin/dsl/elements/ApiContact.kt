/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.dsl.elements

/**
 * The `Contact` information in the OpenAPI schema.
 *
 * @property name The name of the contact person/organization.
 * @property url The URL of the contact person/organization.
 * @property email The email of the contact person/organization.
 */
internal data class ApiContact(
    val name: String? = null,
    val url: String? = null,
    val email: String? = null
)
