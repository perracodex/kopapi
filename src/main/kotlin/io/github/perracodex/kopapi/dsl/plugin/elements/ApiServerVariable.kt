/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.elements

/**
 * Represents an immutable server variable.
 *
 * @property description A human-readable description of the variable.
 * @property defaultValue The default value of the variable.
 * @property choices The possible values of the variable.
 */
internal data class ApiServerVariable(
    val description: String?,
    val defaultValue: String,
    val choices: Set<String>
)
