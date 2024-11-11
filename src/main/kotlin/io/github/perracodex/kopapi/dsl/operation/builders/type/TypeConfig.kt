/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.type

import io.github.perracodex.kopapi.dsl.common.schema.ApiSchemaAttributes
import io.github.perracodex.kopapi.dsl.common.schema.configurable.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.common.schema.configurable.SchemaAttributeDelegate
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.ktor.http.*

/**
 * A builder for appending a type.
 *
 * @property contentType The [ContentType] to assign. Default: `JSON`.
 */
@KopapiDsl
public class TypeConfig @PublishedApi internal constructor(
    private val schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate()
) : ISchemaAttributeConfigurable by schemaAttributeDelegate {
    public var contentType: Set<ContentType>? = null

    /**
     * Returns the registered schema attributes.
     */
    @PublishedApi
    internal fun schemaAttributes(): ApiSchemaAttributes? {
        return schemaAttributeDelegate.attributes
    }

    /**
     * Sets the schema attributes.
     */
    @PublishedApi
    internal fun setSchemaAttributes(attributes: ApiSchemaAttributes?) {
        schemaAttributeDelegate.attributes = attributes
    }
}
