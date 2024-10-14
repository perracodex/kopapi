/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.elements

/**
 * Represents an immutable server variable.
 *
 * #### IMPORTANT
 * The naming, order and nullability of properties are defined
 * as per the OpenAPI specification, so no transformations are
 * needed generating the OpenAPI schema.
 *
 * @property default The default value of the variable.
 * @property enum The possible values of the variable.
 * @property description A human-readable description of the variable.
 */
internal data class ApiServerVariable(
    val default: String,
    val enum: Set<String>?,
    val description: String?,
)
