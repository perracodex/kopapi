/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.schema.delegate

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.schema.builders.SchemaAttributeBuilder

/**
 * Handles the registration of additional schema attributes.
 */
@KopapiDsl
internal interface ISchemaAttributeConfigurable {
    /**
     * Defines additional type constraints and descriptive attributes (e.g., `format`, `pattern`, `maxLength`, etc.).
     *
     * Not applicable to complex object types.
     * For complex objects, use instead the `@schema` annotation directly on the class type.
     *
     * #### Usage
     * ```
     * schema {
     *      pattern = "^[A-Za-z0-9_-]{20,50}$"
     *      minLength = 20
     *      maxLength = 50
     * }
     * ```
     *
     * @receiver [SchemaAttributeBuilder] The builder used to configure the schema attributes.
     */
    fun schema(builder: SchemaAttributeBuilder.() -> Unit)
}