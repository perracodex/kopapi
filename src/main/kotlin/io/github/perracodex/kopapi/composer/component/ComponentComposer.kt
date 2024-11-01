/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.component

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.schema.facets.ElementSchema

/**
 * Composes the `Components` section of the OpenAPI schema.
 */
@ComposerApi
internal object ComponentComposer {

    /**
     * Composes the `Components` section of the OpenAPI schema.
     *
     * @param typeSchemas The set of type schemas to compose.
     * @return The `Components` section of the OpenAPI schema.
     */
    fun compose(typeSchemas: Set<TypeSchema>): Map<String, ElementSchema>? {
        val components: LinkedHashMap<String, ElementSchema> = linkedMapOf()

        typeSchemas.sortedBy { it.name }.forEach { typeSchema ->
            val elementSchema: ElementSchema = transform(typeSchema = typeSchema)
            components[typeSchema.name] = elementSchema
        }

        return components.takeIf { it.isNotEmpty() }
    }

    /**
     * Transforms a `TypeSchema` into an schema component.
     *
     * @param typeSchema The `TypeSchema` to transform.
     * @return The rebuilt transformed schema component.
     */
    private fun transform(typeSchema: TypeSchema): ElementSchema {
        return when (val schema: ElementSchema = typeSchema.schema) {
            is ElementSchema.Object -> {
                val properties: MutableMap<String, ElementSchema> = mutableMapOf()
                val required: MutableSet<String> = mutableSetOf()

                schema.properties.forEach { (name, property) ->
                    // Skip transient properties and rebuild the schema if necessary.
                    if (!property.isTransient) {
                        val rebuiltSchema: ElementSchema = if (property.isNullable) {
                            ElementSchema.Nullable(schema = property.schema)
                        } else {
                            property.schema
                        }
                        properties[name] = rebuiltSchema

                        // Add the property to the required set if it is required.
                        if (property.isRequired) {
                            required.add(name)
                        }
                    }
                }

                // Return a new transformed object schema.
                ElementSchema.TransformedObject(
                    schemaType = schema.schemaType,
                    properties = properties.takeIf { it.isNotEmpty() },
                    required = required.takeIf { it.isNotEmpty() }
                )
            }

            else -> schema // For other schema types, return as-is.
        }
    }
}
