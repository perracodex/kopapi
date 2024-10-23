/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.string.MultilineString

/**
 * Builder for constructing individual parts of a multipart request body.
 *
 * @property name The name of the part.
 * @property required Indicates whether the part is mandatory for the multipart request. Defaults to `true`.
 * @property description Optional description of the part.
 * @property schemaType Optional schema type for the part. Defaults to `string`.
 * @property schemaFormat Optional schema format for the part.
 */
@OperationDsl
public class PartBuilder(
    public val name: String,
    public var required: Boolean = true,
    public var schemaType: ApiType? = null,
    public var schemaFormat: ApiFormat? = null

) {
    public var description: String by MultilineString()
}
