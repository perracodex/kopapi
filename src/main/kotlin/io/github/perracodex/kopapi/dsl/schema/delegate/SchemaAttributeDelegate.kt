/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.schema.delegate

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.schema.builders.SchemaAttributeBuilder
import io.github.perracodex.kopapi.dsl.schema.elements.ApiSchemaAttributes

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
