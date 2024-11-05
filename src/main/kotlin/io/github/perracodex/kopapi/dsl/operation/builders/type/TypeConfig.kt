/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.type

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.ktor.http.*

/**
 * A builder for appending a type.
 *
 * @property contentType The [ContentType] to assign. Defaults to `JSON`.
 */
@KopapiDsl
public class TypeConfig {
    /**
     * The content types associated with the type.
     * Defaults to `ContentType.Application.Json` if not specified.
     */
    public var contentType: Set<ContentType> = setOf(ContentType.Application.Json)
}
