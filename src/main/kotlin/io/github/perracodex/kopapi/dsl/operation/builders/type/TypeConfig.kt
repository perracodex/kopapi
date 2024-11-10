/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.type

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
    public var contentType: Set<ContentType>? = null,

    @Suppress("PropertyName")
    @PublishedApi
    internal val _schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate()
) : ISchemaAttributeConfigurable by _schemaAttributeDelegate
