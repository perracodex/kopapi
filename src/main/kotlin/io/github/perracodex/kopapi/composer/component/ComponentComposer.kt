/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.component

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.inspector.schema.TypeSchema

/**
 * Composes the `Components` section of the OpenAPI schema.
 */
@ComposerAPI
internal object ComponentComposer {

    /**
     * Composes the `Components` section of the OpenAPI schema.
     *
     * @param typeSchemas The set of type schemas to compose.
     * @return The `Components` section of the OpenAPI schema.
     */
    fun compose(typeSchemas: Set<TypeSchema>): Map<String, Any?>? {
        val components: MutableMap<String, Any?> = mutableMapOf()

        typeSchemas.forEach { schema ->
            val transformedSchema: Map<String, Any?> = ComponentTransformer.transform(
                name = schema.name,
                elementSchema = schema.schema
            )
            components.putAll(transformedSchema)
        }

        return components.takeIf { it.isNotEmpty() }
    }
}
