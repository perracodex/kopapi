/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema

import io.github.perracodex.kopapi.schema.ElementSchema

/**
 * Represents a property within an object schema.
 *
 * @property schema The [ElementSchema] for the property.
 * @property isNullable Whether the property is nullable.
 * @property isRequired Whether the property is required.
 * @property isTransient Whether the property is transient, meaning it should be excluded from the schema.
 * @property renamedFrom The original name of the property before renaming. It is `null` if the name was not changed.
 */
internal data class SchemaProperty(
    val schema: ElementSchema,
    val isNullable: Boolean,
    val isRequired: Boolean,
    val isTransient: Boolean,
    val renamedFrom: String?
)
