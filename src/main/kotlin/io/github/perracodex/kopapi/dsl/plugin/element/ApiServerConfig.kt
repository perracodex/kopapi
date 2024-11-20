/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.plugin.element

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig.Variable

/**
 * Represents an immutable server configuration.
 *
 * #### Attention
 * The naming, order and nullability of properties are defined
 * as per the OpenAPI specification, so no transformations are
 * needed generating the OpenAPI schema.
 *
 * @property url The Url of the server.
 * @property description A human-readable description of the server.
 * @property variables Optional map of [Variable] objects representing the server's variables.
 */
internal data class ApiServerConfig(
    val url: String,
    val description: String?,
    val variables: Map<String, Variable>?
) {
    /**
     * Represents an immutable server variable.
     *
     * @property defaultValue Default value for the server variable.
     * @property choices The possible values of the variable.
     * @property description A human-readable description of the variable.
     */
    internal data class Variable(
        @JsonProperty("default") val defaultValue: String,
        @JsonProperty("enum") val choices: Set<String>?,
        val description: String?,
    )
}
