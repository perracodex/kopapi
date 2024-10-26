/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.response

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.schema.ElementSchema
import io.github.perracodex.kopapi.schema.IOpenApiSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.ktor.http.ContentType
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Composes the `responses` section of the OpenAPI schema.
 *
 * @see [ResponseObject]
 * @see [ApiResponse]
 */
@ComposerAPI
internal object ResponseComposer {
    /**
     * Generates the `responses` section of the OpenAPI schema by mapping each API response
     * to its corresponding schema, if applicable.
     *
     * @param responses A map of API response status codes to their corresponding [ApiResponse] objects.
     * @return A map of status codes to [ResponseObject] instances representing the OpenAPI responses.
     */
    fun compose(responses: Map<String, ApiResponse>): Map<String, ResponseObject> {
        val composedResponses: MutableMap<String, ResponseObject> = mutableMapOf()

        responses.forEach { (statusCode, apiResponse) ->
            if (apiResponse.content.isNullOrEmpty()) {
                // No types associated with the response; create a PathResponse without content.
                composedResponses[statusCode] = ResponseObject(
                    description = apiResponse.description,
                    headers = apiResponse.headers,
                    content = null,
                    links = apiResponse.links
                )
                return@forEach
            }

            // Map to hold schemas grouped by their content types.
            val schemasByContentType: MutableMap<ContentType, MutableList<ElementSchema>> = mutableMapOf()

            // Inspect each type and associate its schema with the relevant content types.
            apiResponse.content.forEach { (contentType, types) ->
                types.forEach { type ->
                    val schema: ElementSchema = SchemaRegistry.inspectType(type = type)?.schema
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
                    IOpenApiSchema.determineSchema(
                        composition = apiResponse.composition,
                        schemas = schemas.sortedBy { it.definition }
                    )
                }

            // Create the PathResponse with the composed content.
            composedResponses[statusCode] = ResponseObject(
                description = apiResponse.description,
                headers = apiResponse.headers,
                content = finalContent,
                links = apiResponse.links
            )
        }

        return composedResponses
    }
}
