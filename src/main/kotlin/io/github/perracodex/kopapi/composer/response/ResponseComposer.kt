/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.response

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.dsl.operation.elements.ContentSchema
import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.system.Tracer
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
    private val tracer = Tracer<ResponseComposer>()

    /**
     * Generates the `responses` section of the OpenAPI schema by mapping each API response to its
     * corresponding schema, if applicable.
     *
     * @param responses A map of API response names to their corresponding [ApiResponse] objects.
     * @param inspector The [TypeSchemaProvider] instance used for inspecting types and generating schemas.
     */
    fun compose(
        responses: Map<String, ApiResponse>,
        inspector: TypeSchemaProvider
    ) {
        responses.forEach { (statusCode, apiResponse) ->
            val contentMap: MutableMap<ContentType, List<ContentSchema>> = apiResponse.content
                ?: return@forEach

            contentMap.forEach { (contentType, schemas) ->
                // Inspect each schema inside ContentSchema and process its type.
                val processedSchemas: List<Schema> = schemas.mapNotNull { contentSchema ->
                    inspectType(inspector = inspector, type = contentSchema.type)?.schema
                }

                // Handle the composition of multiple schemas
                if (processedSchemas.isNotEmpty()) {
                    val resolvedSchema: Schema = determineSchema(
                        composition = apiResponse.composition,
                        schemas = processedSchemas
                    )

                    contentMap[contentType] = listOf(ContentSchema(type = schemas.first().type, schema = resolvedSchema))
                } else {
                    tracer.error("No schemas found for response content type: $contentType, status code: $statusCode")
                }
            }
        }
    }

    /**
     * Determines the appropriate [Schema] based on the given composition and a list of `Schema` objects.
     * If only one `schema` is present, it returns that schema directly. If multiple schemas are present,
     * it combines them according to the specified `composition` type, defaulting to `Composition.ANY_OF` if null.
     *
     * @param composition The [Composition] type to apply when combining multiple schemas. Defaults to `Composition.ANY_OF`.
     * @param schemas The list of [Schema] objects to be combined. Assumes the list is non-empty and preprocessed.
     * @return A [Schema] object that may be a single schema or a composite schema based on the provided composition.
     */
    private fun determineSchema(composition: Composition?, schemas: List<Schema>): Schema {
        return if (schemas.size == 1) {
            schemas.first()
        } else {
            when (composition ?: Composition.ANY_OF) {
                Composition.ANY_OF -> Schema.AnyOf(anyOf = schemas)
                Composition.ALL_OF -> Schema.AllOf(allOf = schemas)
                Composition.ONE_OF -> Schema.OneOf(oneOf = schemas)
            }
        }
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