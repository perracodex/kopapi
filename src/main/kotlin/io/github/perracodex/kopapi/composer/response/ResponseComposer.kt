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
import kotlin.collections.orEmpty
import kotlin.collections.set

/**
 * Composes the `responses` section of the OpenAPI schema.
 *
 * @see [ResponseObject]
 * @see [ApiResponse]
 */
@ComposerApi
internal object ResponseComposer {
    private val tracer: Tracer = Tracer<ResponseComposer>()

    /**
     * The key used for the default response in the OpenAPI schema.
     * This is used when no specific status code is defined for a response.
     */
    private const val DEFAULT_KEY: String = "default"

    /**
     * Generates the `responses` section of the OpenAPI schema by mapping each API response
     * to its corresponding schema, if applicable.
     *
     * @param responses A map of API response status codes to their corresponding [ApiResponse] objects.
     * @param defaultResponse An optional default [ApiResponse] to include as a fallback for unspecified status codes.
     * @return A map of status codes to [ResponseObject] instances representing the OpenAPI responses.
     */
    fun compose(responses: Map<HttpStatusCode, ApiResponse>, defaultResponse: ApiResponse?): Map<String, ResponseObject> {
        tracer.info("Composing the 'responses' section of the OpenAPI schema.")

        val composedResponses: MutableMap<String, ResponseObject> = mutableMapOf()

        responses.forEach { (statusCode, apiResponse) ->
            tracer.debug("Composing response: [${statusCode.value}] → ${apiResponse.description}")

            composedResponses[statusCode.value.toString()] = composeSingleResponse(
                code = statusCode.value.toString(),
                apiResponse = apiResponse,
                fallbackDescription = statusCode.description
            )
        }

        defaultResponse?.let { apiResponse ->
            tracer.debug("Composing default response → ${apiResponse.description}")

            composedResponses[DEFAULT_KEY] = composeSingleResponse(
                code = DEFAULT_KEY,
                apiResponse = apiResponse,
                fallbackDescription = HttpStatusCode.OK.description
            )
        }

        tracer.info("Composed ${composedResponses.size} responses.")
        return composedResponses
    }

    /**
     * Composes only the default response for the OpenAPI schema.
     *
     * @param defaultResponse The default [ApiResponse] to be composed.
     * @return A [ResponseObject] representing the default response.
     */
    fun composeDefault(defaultResponse: ApiResponse): Map<String, ResponseObject> {
        tracer.info("Composing the default response.")
        return mapOf(
            "default" to composeSingleResponse(
                code = DEFAULT_KEY,
                apiResponse = defaultResponse,
                fallbackDescription = HttpStatusCode.OK.description
            )
        )
    }

    /**
     * Builds a single [ResponseObject] from an [ApiResponse], using introspection and composition logic.
     *
     * @param code The string representation of the status code or "default".
     * @param apiResponse The response metadata to use.
     * @param fallbackDescription A default description to use if none is explicitly set.
     * @return A fully constructed [ResponseObject].
     */
    private fun composeSingleResponse(
        code: String,
        apiResponse: ApiResponse,
        fallbackDescription: String
    ): ResponseObject {
        val finalHeaders: MutableMap<String, HeaderObject>? = HeaderComposer.compose(
            headers = apiResponse.headers.orEmpty()
        )

        if (apiResponse.content.isNullOrEmpty()) {
            // No content types defined; return response without content.
            return ResponseObject(
                description = apiResponse.description.trimOrNull() ?: fallbackDescription,
                headers = finalHeaders,
                content = null,
                links = apiResponse.links?.toSortedMap()
            )
        }

        val schemasByContentType: MutableMap<ContentType, MutableList<ElementSchema>> = mutableMapOf()

        apiResponse.content.forEach { (contentType, typesDetails) ->
            typesDetails.forEach { details ->
                var baseSchema: ElementSchema = SchemaRegistry.introspectType(type = details.type)?.schema
                    ?: throw KopapiException("No schema found for response type: ${details.type}, status code: $code")

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

        val finalContent: Map<ContentType, OpenApiSchema.ContentSchema> =
            schemasByContentType.toSortedMap(compareBy({ it.contentType }, { it.contentSubtype }))
                .mapValues { (_, schemas) ->
                    CompositionSchema.determine(
                        composition = apiResponse.composition,
                        schemas = schemas.sortedBy { it.definition },
                        examples = apiResponse.examples
                    )
                }

        return ResponseObject(
            description = apiResponse.description.trimOrNull() ?: fallbackDescription,
            headers = finalHeaders,
            content = finalContent,
            links = apiResponse.links?.toSortedMap()
        )
    }
}
