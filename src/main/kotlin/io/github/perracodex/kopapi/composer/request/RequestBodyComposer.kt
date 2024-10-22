/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.request

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.schema.ElementSchema
import io.github.perracodex.kopapi.schema.ISchema
import io.github.perracodex.kopapi.system.KopapiException
import io.ktor.http.*

/**
 * Responsible for composing the `request body` section of the OpenAPI schema.
 *
 * This includes handling both standard request bodies and multipart/form-data bodies,
 * which may have compositions like `ANY_OF`, `ALL_OF`, and `ONE_OF`.
 */
@ComposerAPI
internal object RequestBodyComposer {
    /**
     * Generates the `request body` section of the OpenAPI schema.
     *
     * This method processes both standard request bodies and multipart request bodies.
     *
     * @param requestBody The [ApiRequestBody] object containing the request body metadata.
     * @return The constructed [PathRequestBody] object representing the OpenAPI request body.
     */
    fun compose(requestBody: ApiRequestBody): PathRequestBody {
        val schemasByContentType: MutableMap<ContentType, MutableList<ElementSchema>> = mutableMapOf()

        // Handling standard content types (non-multipart)
        requestBody.content?.forEach { (contentType, types) ->
            types.forEach { type ->
                val schema: ElementSchema = SchemaRegistry.inspectType(type = type)?.schema
                    ?: throw KopapiException("No schema found for type: $type")
                schemasByContentType.getOrPut(contentType) { mutableListOf() }.add(schema)
            }
        }

        // Collected schemas for multipart content types
        val multipartSchema: Map<ContentType, OpenAPiSchema.ContentSchema>? = requestBody
            .multipartContent?.let { parts ->
                parts.mapValues { (_, schema) ->
                    OpenAPiSchema.ContentSchema(schema = schema)
                }
            }

        // Combining schemas for standard content types
        val standardSchema: Map<ContentType, OpenAPiSchema.ContentSchema> = schemasByContentType
            .mapValues { (_, schemas) ->
                ISchema.determineSchema(composition = requestBody.composition, schemas = schemas)
            }

        // Building the final request body object by combining standard and multipart content.
        val content: Map<ContentType, OpenAPiSchema.ContentSchema> = multipartSchema?.let {
            standardSchema + it
        } ?: standardSchema

        return PathRequestBody(
            description = requestBody.description,
            required = requestBody.required,
            content = content
        )
    }
}
