/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.ktor.http.*

/**
 * Builder for constructing individual parts of a multipart request body.
 *
 * @property name The name of the part.
 * @property required Indicates whether the part is mandatory for the multipart request. Default: `true`.
 * @property description Optional description of the part.
 * @property contentType Optional content type for the part.
 * @property schemaType Optional schema type for the part. Default: `string`.
 * @property schemaFormat Optional schema format for the part.
 */
@KopapiDsl
public class PartBuilder(
    public val name: String,
    public var required: Boolean = true,
    public var contentType: ContentType? = null,
    public var schemaType: ApiType? = null,
    public var schemaFormat: ApiFormat? = null

) {
    public var description: String by MultilineString()
}
