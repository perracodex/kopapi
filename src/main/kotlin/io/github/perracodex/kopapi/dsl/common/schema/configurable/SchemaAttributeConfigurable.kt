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
internal class SchemaAttributeConfigurable : ISchemaAttributeConfigurable {
    /** Cached schema attributes. */
    var attributes: ApiSchemaAttributes? = null

    override fun schema(configure: SchemaAttributeBuilder.() -> Unit) {
        attributes = SchemaAttributeBuilder().apply(configure).build()
    }
}
