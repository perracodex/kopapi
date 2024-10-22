/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.utils.string.MultilineString

/**
 * Builder for constructing individual parts of a multipart request body.
 *
 * @property name The name of the part.
 * @property required Indicates whether the part is mandatory for the multipart request. Defaults to `true`.
 * @property description Optional description of the part.
 * @property deprecated Indicates whether the part is deprecated. Defaults to `false`.
 */
@OperationDsl
public class PartBuilder @PublishedApi internal constructor(
    public val name: String,
    public var required: Boolean = true,
    public var deprecated: Boolean = false
) {
    public var description: String by MultilineString()
}
