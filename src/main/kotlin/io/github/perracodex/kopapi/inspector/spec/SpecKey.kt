/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.spec

/**
 * Represents the keys used in OpenAPI specifications.
 *
 * @property value The string value of the key.
 */
internal enum class SpecKey(val value: String) {
    ENUM(value = "enum"),
    FORMAT(value = "format"),
    TYPE(value = "type"),
    ITEMS(value = "items"),
    PROPERTIES(value = "properties"),
    ADDITIONAL_PROPERTIES(value = "additionalProperties"),
    REQUIRED(value = "required"),
    NULLABLE(value = "nullable"),
    MIN_LENGTH(value = "minLength"),
    MAX_LENGTH(value = "maxLength"),
    REFERENCE(value = "\$ref"),
    TRANSIENT(value = "transient"), // For internal use, not part of OpenAPI spec.
    ORIGINAL_NAME(value = "originalName"); // For internal use, not part of OpenAPI spec.

    operator fun invoke(): String = value

    override fun toString(): String = value
}
