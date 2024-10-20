/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.request

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.ktor.http.*
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Composes the `request body` section of the OpenAPI schema.
 *
 * @see [ApiRequestBody]
 */
@ComposerAPI
internal object RequestBodyComposer {
    /**
     * Generates the `request body` section of the OpenAPI schema.
     *
     * @param requestBody The [ApiRequestBody] object containing the request body metadata.
     * @return The processed [PathRequestBody] object.
     */
    fun compose(requestBody: ApiRequestBody): PathRequestBody {
        val schemasByContentType: MutableMap<ContentType, MutableList<Schema>> = mutableMapOf()

        // Inspect each type and associate its schema with the relevant content types.
        requestBody.content.forEach { (contentType, types) ->
            types.forEach { type ->
                val schema: Schema = SchemaRegistry.inspectType(type = type)?.schema
                    ?: throw KopapiException("No schema found for type: $type")

                schemasByContentType
                    .getOrPut(contentType) { mutableListOf() }
                    .add(schema)
            }
        }

        // Build the final content map with combined schemas per content type.
        val finalContent: Map<ContentType, OpenAPiSchema.ContentSchema> = schemasByContentType
            .toSortedMap(compareBy({ it.contentType }, { it.contentSubtype }))
            .mapValues { (_, schemas) ->
                determineSchema(
                    composition = requestBody.composition ?: Composition.ANY_OF,
                    schemas = schemas.sortedBy { it.ordinal }
                )
            }

        return PathRequestBody(
            description = requestBody.description,
            required = requestBody.required,
            deprecated = requestBody.deprecated,
            content = finalContent
        )
    }

    /**
     * Determines the appropriate [OpenAPiSchema.ContentSchema] based on the given composition
     * and a list of `Schema` objects.
     *
     * - If only one schema is present, it returns that schema directly.
     * - If multiple schemas are present, it combines them according to
     *   the specified `composition` type, defaulting to `Composition.ANY_OF`.
     *
     * @param composition The [Composition] type to apply when combining multiple schemas.
     *                    Defaults to `Composition.ANY_OF` if null.
     * @param schemas The list of [Schema] objects to be combined. Assumes the list is non-empty and preprocessed.
     * @return An [OpenAPiSchema.ContentSchema] representing the combined schema.
     */
    private fun determineSchema(composition: Composition?, schemas: List<Schema>): OpenAPiSchema.ContentSchema {
        val combinedSchema: Schema = when {
            schemas.size == 1 -> schemas.first()
            else -> when (composition ?: Composition.ANY_OF) {
                Composition.ANY_OF -> Schema.AnyOf(anyOf = schemas)
                Composition.ALL_OF -> Schema.AllOf(allOf = schemas)
                Composition.ONE_OF -> Schema.OneOf(oneOf = schemas)
            }
        }

        return OpenAPiSchema.ContentSchema(schema = combinedSchema)
    }
}
