/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.response

import io.github.perracodex.kopapi.annotation.SchemaAttributeUtils
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.header.HeaderComposer
import io.github.perracodex.kopapi.composer.header.HeaderObject
import io.github.perracodex.kopapi.dsl.operation.element.ApiResponse
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.schema.facet.CompositionSchema
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.trimOrNull
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
            val finalHeaders: MutableMap<String, HeaderObject>? = HeaderComposer.compose(
                headers = apiResponse.headers.orEmpty()
            )

            if (apiResponse.content.isNullOrEmpty()) {
                // No types associated with the response; create a ResponseObject without content.
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
            apiResponse.content.forEach { (contentType, typesDetails) ->
                typesDetails.forEach { details ->
                    var baseSchema: ElementSchema = SchemaRegistry.introspectType(type = details.type)?.schema
                        ?: throw KopapiException(
                            "No schema found for response type: ${details.type}, status code: $statusCode"
                        )

                    // Apply additional parameter attributes.
                    details.schemaAttributes?.let { attributes ->
                        baseSchema = SchemaAttributeUtils.copySchemaAttributes(
                            schema = baseSchema,
                            attributes = attributes
                        )
                    }

                    schemasByContentType.getOrPut(contentType) {
                        mutableListOf()
                    }.add(baseSchema)
                }
            }

            // Build the final content map with combined schemas per content type.
            val finalContent: Map<ContentType, OpenApiSchema.ContentSchema> = schemasByContentType
                .toSortedMap(compareBy({ it.contentType }, { it.contentSubtype }))
                .mapValues { (_, schemas) ->
                    CompositionSchema.determine(
                        composition = apiResponse.composition,
                        schemas = schemas.sortedBy { it.definition },
                        examples = apiResponse.examples
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
}
