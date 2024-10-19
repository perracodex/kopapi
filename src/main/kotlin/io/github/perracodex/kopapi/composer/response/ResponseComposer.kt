/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.response

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.ktor.http.*
import kotlin.collections.set
import kotlin.reflect.KType

/**
 * Responsible for composing the `responses` section of the OpenAPI schema.
 *
 * The `responses` section maps each API response to its corresponding schema, if applicable.
 *
 * @see [ApiResponse]
 */
@ComposerAPI
internal object ResponseComposer {

    /**
     * Generates the `responses` section of the OpenAPI schema by mapping each API response to its
     * corresponding schema, if applicable.
     *
     * @param responses A map of API response names to their corresponding [ApiResponse] objects.
     * @param inspector The [TypeSchemaProvider] instance used for inspecting types and generating schemas.
     * @return A map of status codes to [PathResponse] objects.
     */
    fun compose(
        responses: Map<String, ApiResponse>,
        inspector: TypeSchemaProvider
    ): Map<String, PathResponse> {
        val pathResponses: MutableMap<String, PathResponse> = mutableMapOf()

        responses.forEach { (statusCode, apiResponse) ->
            // If there are no types, skip processing and continue to the next iteration.
            if (apiResponse.types.isNullOrEmpty()) {
                pathResponses[statusCode] = PathResponse(
                    description = apiResponse.description,
                    headers = apiResponse.headers,
                    content = null,
                    links = apiResponse.links,
                )
                return@forEach
            }

            // Initialize a map to store schemas per content type.
            val contentMap: MutableMap<ContentType, MutableList<Schema>> = mutableMapOf()

            // Collect schemas for each type and content type.
            apiResponse.types.forEach { (type, contentTypes) ->
                val inspectedSchema: Schema? = inspectType(
                    inspector = inspector,
                    type = type
                )?.schema

                // Add the schema for each associated content type.
                inspectedSchema?.let { schema ->
                    contentTypes.forEach { contentType ->
                        contentMap.computeIfAbsent(contentType) { mutableListOf() }.add(schema)
                    }
                } ?: throw KopapiException("No schema found for type: $type, status code: $statusCode")
            }

            // Create the final content map for ApiResponse
            val finalContent: MutableMap<ContentType, OpenAPiSchema.ContentSchema> = mutableMapOf()

            // Process each content type's collected schemas.
            contentMap.toSortedMap(compareBy({ it.contentType }, { it.contentSubtype }))
                .forEach { (contentType, schemas) ->
                    finalContent[contentType] = determineSchema(
                        composition = apiResponse.composition,
                        schemas = schemas.sortedBy { it.ordinal }
                    )
                }

            // Set the final processed content map to the ApiResponse.
            pathResponses[statusCode] = PathResponse(
                description = apiResponse.description,
                headers = apiResponse.headers,
                content = finalContent,
                links = apiResponse.links,
            )
        }

        return pathResponses
    }

    /**
     * Determines the appropriate [OpenAPiSchema.ContentSchema] based on the given composition and a list of `Schema` objects.
     * If only one `schema` is present, it returns that schema directly. If multiple schemas are present,
     * it combines them according to the specified `composition` type, defaulting to `Composition.ANY_OF` if null.
     *
     * @param composition The [Composition] type to apply when combining multiple schemas. Defaults to `Composition.ANY_OF`.
     * @param schemas The list of [Schema] objects to be combined. Assumes the list is non-empty and preprocessed.
     * @return A [Schema] object that may be a single schema or a composite schema based on the provided composition.
     */
    private fun determineSchema(composition: Composition?, schemas: List<Schema>): OpenAPiSchema.ContentSchema {
        val schema: Schema = if (schemas.size == 1) {
            schemas.first()
        } else {
            when (composition ?: Composition.ANY_OF) {
                Composition.ANY_OF -> Schema.AnyOf(anyOf = schemas)
                Composition.ALL_OF -> Schema.AllOf(allOf = schemas)
                Composition.ONE_OF -> Schema.OneOf(oneOf = schemas)
            }
        }

        return OpenAPiSchema.ContentSchema(schema = schema)
    }

    /**
     * Inspects a type using the provided [TypeSchemaProvider] if it's not of type [Unit].
     *
     * @param inspector The [TypeSchemaProvider] instance used for inspection.
     * @param type The [KType] to inspect.
     * @return The [TypeSchema] object representing the inspected type, or `null` if the type is [Unit].
     */
    private fun inspectType(inspector: TypeSchemaProvider, type: KType): TypeSchema? {
        if (type.classifier != Unit::class) {
            return inspector.inspect(kType = type)
        }
        return null
    }
}
