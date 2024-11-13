/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.component

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.introspector.schema.TypeSchema
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.schema.facet.ISchemaFacet
import io.github.perracodex.kopapi.schema.facet.NullableSchema
import io.github.perracodex.kopapi.schema.facet.ObjectSchema
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.trimOrNull

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
    fun compose(typeSchemas: Set<TypeSchema>): Map<String, ISchemaFacet>? {
        val components: LinkedHashMap<String, ISchemaFacet> = linkedMapOf()

        typeSchemas.sortedBy { it.name }.forEach { typeSchema ->
            val schemaFacet: ISchemaFacet = transform(typeSchema = typeSchema)
            components[typeSchema.name] = schemaFacet
        }

        return components.orNull()
    }

    /**
     * Transforms a `TypeSchema` into an schema component.
     *
     * @param typeSchema The `TypeSchema` to transform.
     * @return The rebuilt transformed schema component.
     */
    private fun transform(typeSchema: TypeSchema): ISchemaFacet {
        return when (val schema: ElementSchema = typeSchema.schema) {
            is ElementSchema.ObjectDescriptor -> {
                val properties: MutableMap<String, ISchemaFacet> = mutableMapOf()
                val required: MutableSet<String> = mutableSetOf()

                schema.objectProperties.forEach { (name, property) ->
                    // Skip transient properties and rebuild the schema if necessary.
                    if (!property.isTransient) {
                        val rebuiltSchema: ISchemaFacet = if (property.isNullable) {
                            NullableSchema.create(schemaProperty = property)
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
                ObjectSchema(
                    description = schema.description.trimOrNull(),
                    defaultValue = schema.defaultValue,
                    examples = schema.examples,
                    properties = properties.orNull(),
                    required = required.orNull()
                )
            }

            else -> schema // For other schema types, return as-is.
        }
    }
}
