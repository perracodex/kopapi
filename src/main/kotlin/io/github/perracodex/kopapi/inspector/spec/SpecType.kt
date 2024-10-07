/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.spec

/**
 * Represents the types used in OpenAPI specifications.
 *
 * @property value The string value of the type.
 */
internal enum class SpecType(val value: String) {
    ARRAY(value = "array"),
    BOOLEAN(value = "boolean"),
    INTEGER(value = "integer"),
    NUMBER(value = "number"),
    OBJECT(value = "object"),
    STRING(value = "string");

    operator fun invoke(): String = value

    override fun toString(): String = value
}