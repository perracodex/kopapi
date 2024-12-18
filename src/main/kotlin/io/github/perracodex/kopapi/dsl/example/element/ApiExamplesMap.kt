/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.example.element

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Represents a map of named examples.
 */
internal data class ApiExamplesMap(
    @JsonValue val examples: Map<String, ApiExample>
) : IExample
