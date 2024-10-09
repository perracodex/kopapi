/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema

/**
 * Represents a property within an object schema.
 *
 * @property schema The [Schema] for the property.
 * @property isNullable Whether the property is nullable.
 * @property isRequired Whether the property is required.
 * @property originalName The original name of the property, if different from the name.
 * @property isTransient Whether the property is transient, meaning it should be excluded from the schema.
 */
internal data class SchemaProperty(
    val schema: Schema,
    val isNullable: Boolean = false,
    val isRequired: Boolean = true,
    val originalName: String? = null,
    val isTransient: Boolean = false
)
