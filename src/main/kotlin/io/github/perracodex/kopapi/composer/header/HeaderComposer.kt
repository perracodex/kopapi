/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.header

import io.github.perracodex.kopapi.annotation.SchemaAttributeUtils
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.operation.element.ApiHeader
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.trimOrNull
import io.ktor.http.*

/**
 * Composes the `header` sections for the OpenAPI schema.
 */
@ComposerApi
internal object HeaderComposer {
    /**
     * Converts a map of [ApiHeader] instances to a map of OpenAPI header objects.
     */
    fun compose(headers: Map<String, ApiHeader>): MutableMap<String, HeaderObject>? {
        if (headers.isEmpty()) {
            return null
        }

        val headerObjects: MutableMap<String, HeaderObject> = mutableMapOf()

        headers.forEach { (name: String, header: ApiHeader) ->
            // Determine the schema for the header, and introspect accordingly.
            var baseSchema: ElementSchema = SchemaRegistry.introspectType(type = header.type)?.schema
                ?: throw KopapiException("No schema found for header type: ${header.type}")

            // Apply additional header attributes.
            header.schemaAttributes?.let {
                baseSchema = SchemaAttributeUtils.copySchemaAttributes(
                    schema = baseSchema,
                    attributes = header.schemaAttributes
                )
            }

            // Determine the content schema if the header requires a specific media format.
            val content: Map<ContentType, OpenApiSchema.ContentSchema>? = header.contentType?.let { contentType ->
                val contentSchema: OpenApiSchema.ContentSchema = OpenApiSchema.ContentSchema(
                    schema = baseSchema,
                    examples = null
                )
                mapOf(contentType to contentSchema)
            }

            // Construct the header object.
            val headerObject = HeaderObject(
                description = header.description.trimOrNull(),
                required = header.required,
                explode = header.explode.takeIf { it == true },
                schema = baseSchema.takeIf { content == null },
                content = content,
                deprecated = header.deprecated.takeIf { it == true }
            )
            headerObjects[name] = headerObject
        }

        return headerObjects.orNull()
    }
}
