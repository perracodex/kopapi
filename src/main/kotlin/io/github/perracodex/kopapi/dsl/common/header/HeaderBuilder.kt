/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.header

import io.github.perracodex.kopapi.dsl.common.schema.configurable.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.common.schema.configurable.SchemaAttributeDelegate
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Builds a response header for an API endpoint.
 *
 * @property description A human-readable description of the header.
 * @property required Indicates whether the header is mandatory.
 * @property explode Indicates whether arrays and objects are serialized as a single comma-separated header.
 * @property contentType Optional [ContentType] when a specific media format is required.
 * @property deprecated Indicates whether the header is deprecated and should be avoided.
 *
 * @see [ResponseBuilder]
 * @see [HeaderBuilder]
 */
@KopapiDsl
public class HeaderBuilder @PublishedApi internal constructor(
    private val schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate()
) : ISchemaAttributeConfigurable by schemaAttributeDelegate {
    public var description: String by MultilineString()
    public var required: Boolean = true
    public var deprecated: Boolean = false
    public var explode: Boolean = false
    public var contentType: ContentType? = null

    /**
     * Builds an [ApiHeader] instance from the current builder state.
     *
     * @param type The [KType] of the header.
     * @return The constructed [ApiHeader] instance.
     */
    @PublishedApi
    internal fun build(type: KType): ApiHeader {
        return ApiHeader(
            type = type,
            description = description.trimOrNull(),
            required = required,
            explode = explode.takeIf { it },
            contentType = contentType,
            schemaAttributes = schemaAttributeDelegate.attributes,
            deprecated = deprecated.takeIf { it }
        )
    }
}
