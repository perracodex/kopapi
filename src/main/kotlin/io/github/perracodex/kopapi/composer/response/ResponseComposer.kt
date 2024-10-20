/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.response

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.ktor.http.ContentType
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Composes the `responses` section of the OpenAPI schema.
 *
 * @see [ApiResponse]
 */
@ComposerAPI
internal object ResponseComposer {
    /**
     * Generates the `responses` section of the OpenAPI schema by mapping each API response
     * to its corresponding schema, if applicable.
     *
     * @param responses A map of API response status codes to their corresponding [ApiResponse] objects.
     * @return A map of status codes to [PathResponse] objects representing the OpenAPI responses.
     */
    fun compose(responses: Map<String, ApiResponse>): Map<String, PathResponse> {
        val composedResponses: MutableMap<String, PathResponse> = mutableMapOf()

        responses.forEach { (statusCode, apiResponse) ->
            if (apiResponse.content.isNullOrEmpty()) {
                // No types associated with the response; create a PathResponse without content.
                composedResponses[statusCode] = PathResponse(
                    description = apiResponse.description,
                    headers = apiResponse.headers,
                    content = null,
                    links = apiResponse.links
                )
                return@forEach
            }

            // Map to hold schemas grouped by their content types.
            val schemasByContentType: MutableMap<ContentType, MutableList<Schema>> = mutableMapOf()

            // Inspect each type and associate its schema with the relevant content types.
            apiResponse.content.forEach { (contentType, types) ->
                types.forEach { type ->
                    val schema: Schema = SchemaRegistry.inspectType(type = type)?.schema
                        ?: throw KopapiException("No schema found for type: $type, status code: $statusCode")

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
                        composition = apiResponse.composition,
                        schemas = schemas.sortedBy { it.ordinal }
                    )
                }

            // Create the PathResponse with the composed content.
            composedResponses[statusCode] = PathResponse(
                description = apiResponse.description,
                headers = apiResponse.headers,
                content = finalContent,
                links = apiResponse.links
            )
        }

        return composedResponses
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
