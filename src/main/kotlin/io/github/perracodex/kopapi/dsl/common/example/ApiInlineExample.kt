/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.example

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Represents an example for inline use in arrays, without metadata.
 *
 * @property value The value of the example.
 */
internal data class ApiInlineExample(
    @JsonValue val value: Any?
) : IExample
