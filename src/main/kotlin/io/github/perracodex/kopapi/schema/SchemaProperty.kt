/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema

/**
 * Represents a property within an `object` schema.
 *
 * Used when traversing the schema to keep track of the property's metadata.
 *
 * @property schema The [ElementSchema] for the property.
 * @property isNullable Whether the property is nullable.
 * @property isRequired Whether the property is required.
 * @property isTransient Whether the property is transient, meaning it should be excluded from the schema.
 * @property renamedFrom The original name of the property if such changed occurred due to an annotation.
 */
internal data class SchemaProperty(
    val schema: ElementSchema,
    val isNullable: Boolean,
    val isRequired: Boolean,
    val isTransient: Boolean,
    val renamedFrom: String?
)
