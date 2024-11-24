/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.operation.builder.type

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.schema.delegate.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.schema.delegate.SchemaAttributeDelegate
import io.github.perracodex.kopapi.dsl.schema.element.ApiSchemaAttributes
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
     * Schema attributes configuration.
     */
    @Suppress("PropertyName", "VariableNaming")
    @PublishedApi
    internal var _schemaAttributes: ApiSchemaAttributes?
        get() = schemaAttributeDelegate.attributes
        set(value) {
            schemaAttributeDelegate.attributes = value
        }
}
