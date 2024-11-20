/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.schema.delegate

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.schema.builder.SchemaAttributeBuilder
import io.github.perracodex.kopapi.dsl.schema.element.ApiSchemaAttributes

/**
 * Handles the registration of schema attributes.
 */
@KopapiDsl
internal class SchemaAttributeDelegate : ISchemaAttributeConfigurable {
    /** Cached schema attributes. */
    var attributes: ApiSchemaAttributes? = null

    override fun schema(builder: SchemaAttributeBuilder.() -> Unit) {
        attributes = SchemaAttributeBuilder().apply(builder).build()
    }
}
