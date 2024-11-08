/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.attributes

import io.github.perracodex.kopapi.dsl.common.primitive.ApiPrimitiveTypeSchema
import io.github.perracodex.kopapi.dsl.common.primitive.PrimitiveTypeSchemaBuilder
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
    public var required: Boolean = true,
    public var deprecated: Boolean = false,
    public var explode: Boolean = false,
    public var contentType: ContentType? = null
) {
    public var description: String by MultilineString()

    /** Cached primitive type schema. */
    private var _primitiveTypeSchema: ApiPrimitiveTypeSchema? = null

    /**
     * Defines additional properties for a primitive header type.
     *
     * Use this function to specify constraints and descriptive attributes (e.g., `format`, `pattern`,
     * `maxLength`) applicable to primitive types like strings and numbers.
     *
     * This configuration does not apply to complex object types.
     *
     * #### Sample usage
     * ```
     * header<String>(name = "X-Session-Token") {
     *     description = "The session token for the user."
     *     schema {
     *         pattern = "^[A-Za-z0-9_-]{20,50}$"
     *         minLength = 20
     *         maxLength = 50
     *     }
     * }
     * ```
     *
     * @param configure A lambda for setting properties in [PrimitiveTypeSchemaBuilder]
     * that refine the primitive type's attributes.
     */
    public fun schema(configure: PrimitiveTypeSchemaBuilder.() -> Unit) {
        _primitiveTypeSchema = PrimitiveTypeSchemaBuilder().apply(configure).build()
    }

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
            primitiveTypeSchema = _primitiveTypeSchema,
            deprecated = deprecated.takeIf { it }
        )
    }
}
