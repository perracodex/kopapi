/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.schema.configurable

import io.github.perracodex.kopapi.dsl.common.schema.ApiSchemaAttributes
import io.github.perracodex.kopapi.dsl.common.schema.SchemaAttributeBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl

/**
 * Handles the registration of schema attributes.
 */
@KopapiDsl
@PublishedApi
internal class SchemaAttributeDelegate : ISchemaAttributeConfigurable {
    /** Cached schema attributes. */
    @PublishedApi
    internal var attributes: ApiSchemaAttributes? = null

    override fun schema(builder: SchemaAttributeBuilder.() -> Unit) {
        attributes = SchemaAttributeBuilder().apply(builder).build()
    }
}
