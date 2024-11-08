/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.schema.configurable

import io.github.perracodex.kopapi.dsl.common.schema.SchemaAttributeBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl

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
     * #### Sample usage
     * ```
     * schema {
     *      pattern = "^[A-Za-z0-9_-]{20,50}$"
     *      minLength = 20
     *      maxLength = 50
     * }
     * ```
     *
     * @param configure A lambda for setting properties in [SchemaAttributeBuilder] that refine the type's attributes.
     *
     * @see [SchemaAttributeBuilder]
     */
    fun schema(configure: SchemaAttributeBuilder.() -> Unit)
}