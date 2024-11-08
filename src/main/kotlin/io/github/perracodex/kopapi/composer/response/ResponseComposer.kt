/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.response

import io.github.perracodex.kopapi.composer.SchemaComposer
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.schema.facets.ElementSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Composes the `responses` section of the OpenAPI schema.
 *
 * @see [ResponseObject]
 * @see [ApiResponse]
 */
@ComposerApi
internal object ResponseComposer {
    private val tracer = Tracer<ResponseComposer>()

    /**
     * Generates the `responses` section of the OpenAPI schema by mapping each API response
     * to its corresponding schema, if applicable.
     *
     * @param responses A map of API response status codes to their corresponding [ApiResponse] objects.
     * @return A map of status codes to [ResponseObject] instances representing the OpenAPI responses.
     */
    fun compose(responses: Map<HttpStatusCode, ApiResponse>): Map<String, ResponseObject> {
        tracer.info("Composing the 'responses' section of the OpenAPI schema.")

        val composedResponses: MutableMap<String, ResponseObject> = mutableMapOf()

        responses.forEach { (statusCode, apiResponse) ->
            tracer.debug("Composing response: [${statusCode.value}] → ${apiResponse.description}")

            // Process the headers for the response.
            val finalHeaders: MutableMap<String, HeaderObject>? = processHeaders(
                headers = apiResponse.headers.orEmpty()
            )

            if (apiResponse.content.isNullOrEmpty()) {
                // No types associated with the response; create a PathResponse without content.
                composedResponses[statusCode.value.toString()] = ResponseObject(
                    description = apiResponse.description,
                    headers = finalHeaders,
                    content = null,
                    links = apiResponse.links?.toSortedMap()
                )
                return@forEach
            }

            // Map to hold schemas grouped by their content types.
            val schemasByContentType: MutableMap<ContentType, MutableList<ElementSchema>> = mutableMapOf()

            // Introspect each type and associate its schema with the relevant content types.
            apiResponse.content.forEach { (contentType, types) ->
                types.forEach { type ->
                    val schema: ElementSchema = SchemaRegistry.introspectType(type = type)?.schema
                        ?: throw KopapiException("No schema found for type: $type, status code: $statusCode")

                    schemasByContentType
                        .getOrPut(contentType) { mutableListOf() }
                        .add(schema)
                }
            }

            // Build the final content map with combined schemas per content type.
            val finalContent: Map<ContentType, OpenApiSchema.ContentSchema> = schemasByContentType
                .toSortedMap(compareBy({ it.contentType }, { it.contentSubtype }))
                .mapValues { (_, schemas) ->
                    SchemaComposer.determineSchema(
                        composition = apiResponse.composition,
                        schemas = schemas.sortedBy { it.definition }
                    )
                }

            // Create the PathResponse with the composed content.
            composedResponses[statusCode.value.toString()] = ResponseObject(
                description = apiResponse.description.trimOrNull() ?: statusCode.description,
                headers = finalHeaders,
                content = finalContent,
                links = apiResponse.links?.toSortedMap()
            )
        }

        tracer.info("Composed ${composedResponses.size} responses.")

        return composedResponses
    }

    /**
     * Converts a map of [ApiHeader] instances to a map of OpenAPI header objects.
     */
    private fun processHeaders(headers: Map<String, ApiHeader>): MutableMap<String, HeaderObject>? {
        val headerObjects: MutableMap<String, HeaderObject> = mutableMapOf()

        headers.forEach { (nam: String, header: ApiHeader) ->
            // Determine the schema for the header, and introspect accordingly.
            var schema: ElementSchema = SchemaRegistry.introspectType(type = header.type)?.schema
                ?: throw KopapiException("No schema found for header type: ${header.type}")

            // If the header is a primitive type, apply the pattern if specified.
            // This is only meaningful for string headers, but we don't verify it
            // as is the responsibility of the developer to provide a valid schema.
            if (schema is ElementSchema.Primitive) {
                header.pattern?.let { pattern ->
                    schema = (schema as ElementSchema.Primitive).copy(
                        pattern = pattern,
                    )
                }
            }

            // Determine the content schema if the header requires a specific media format.
            val content: Map<ContentType, OpenApiSchema.ContentSchema>? = header.contentType?.let { contentType ->
                mapOf(contentType to OpenApiSchema.ContentSchema(schema = schema))
            }

            // Construct the header object.
            val headerObject = HeaderObject(
                description = header.description.trimOrNull(),
                required = header.required,
                explode = header.explode.takeIf { it == true },
                schema = schema.takeIf { content == null },
                content = content,
                deprecated = header.deprecated.takeIf { it == true }
            )
            headerObjects[nam] = headerObject
        }

        return headerObjects.takeIf { it.isNotEmpty() }
    }
}
