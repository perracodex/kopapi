/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.elements

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents an immutable server variable.
 *
 * @property defaultValue The default value of the variable.
 * @property choices The possible values of the variable.
 * @property description A human-readable description of the variable.
 */
internal data class ApiServerVariable(
    @JsonProperty("default") val defaultValue: String,
    @JsonProperty("enum") val choices: Set<String>?,
    val description: String?,
)
