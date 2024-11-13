/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.example.element

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Represents an example for inline use in arrays, without metadata.
 *
 * @property value The value of the example.
 */
internal data class ApiInlineExample(
    @JsonValue
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val value: Any?
) : IExample
