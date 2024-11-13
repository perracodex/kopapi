/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.example.element

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Represents an example for a schema with optional metadata.
 *
 * @property summary A brief summary of the example.
 * @property description A detailed description of the example.
 * @property value The value of the example.
 */
internal data class ApiExample(
    val summary: String? = null,
    val description: String? = null,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val value: Any?
) : IExample
