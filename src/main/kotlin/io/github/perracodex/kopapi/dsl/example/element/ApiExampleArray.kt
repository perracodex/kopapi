/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.example.element

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Represents multiple examples within a single wrapper.
 *
 * @property examples An array of examples.
 */
internal data class ApiExampleArray(
    @JsonValue val examples: Array<IExample>,
) : IExample {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ApiExampleArray
        return examples.contentEquals(other.examples)
    }

    override fun hashCode(): Int {
        return examples.contentHashCode()
    }
}
