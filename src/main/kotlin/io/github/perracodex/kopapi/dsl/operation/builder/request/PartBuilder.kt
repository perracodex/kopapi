/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.operation.builder.request

import io.github.perracodex.kopapi.dsl.header.delegate.HeaderDelegate
import io.github.perracodex.kopapi.dsl.header.delegate.IHeaderConfigurable
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.element.ApiHeader
import io.github.perracodex.kopapi.type.ApiType
import io.github.perracodex.kopapi.util.string.MultilineString
import io.ktor.http.*

/**
 * Builder for constructing individual parts of a multipart request body.
 *
 * @property name The name of the part.
 * @property required Indicates whether the part is mandatory for the multipart request. Default: `true`.
 * @property description Optional description of the part.
 * @property contentType Optional set of [ContentType]s for the part.
 * @property schemaType Optional schema type for the part. Default: `string`.
 * @property schemaFormat Optional schema format for the part
 */
@KopapiDsl
public class PartBuilder @PublishedApi internal constructor(
    public val name: String,
    private val headerDelegate: HeaderDelegate = HeaderDelegate()
) : IHeaderConfigurable by headerDelegate {

    public var required: Boolean = true
    public var description: String by MultilineString()
    public var contentType: Set<ContentType>? = null
    public var schemaType: ApiType? = null
    public var schemaFormat: String? = null

    /**
     * Returns the registered headers.
     */
    @PublishedApi
    internal fun headers(): Map<String, ApiHeader>? {
        return headerDelegate.build()
    }
}
