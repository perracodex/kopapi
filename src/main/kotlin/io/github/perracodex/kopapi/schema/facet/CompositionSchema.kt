/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema.facet

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.type.Composition
import io.github.perracodex.kopapi.util.safeName

/**
 * Represents composition schemas that combine multiple element schemas based on composition rules.
 *
 * This sealed class defines schemas such as `anyOf`, `allOf`, and `oneOf` in OpenAPI.
 * These schemas allow combining or constraining the data using multiple schema rules.
 *
 * @property definition A unique identifier for debugging and clarity during schema generation.
 */
@ComposerApi
internal sealed class CompositionSchema(
    @JsonIgnore open val definition: String,
) : ISchemaFacet {
    /**
     * Represents a schema that allows for one or more schemas to be used interchangeably.
     *
     * @property anyOf A list of schemas, any of which can validate the data.
     */
    data class AnyOf(
        @JsonIgnore override val definition: String = AnyOf::class.safeName(),
        @JsonProperty("anyOf") val anyOf: List<ElementSchema>
    ) : CompositionSchema(definition = definition)

    /**
     * Represents a schema that requires all listed schemas to be validated against the data.
     *
     * @property allOf A list of schemas, all of which must validate the data.
     */
    data class AllOf(
        @JsonIgnore override val definition: String = AllOf::class.safeName(),
        @JsonProperty("allOf") val allOf: List<ElementSchema>
    ) : CompositionSchema(definition = definition)

    /**
     * Represents a schema where data must match exactly one of the listed schemas.
     *
     * @property oneOf A list of schemas, one of which must validate the data.
     */
    data class OneOf(
        @JsonIgnore override val definition: String = OneOf::class.safeName(),
        @JsonProperty("oneOf") val oneOf: List<ElementSchema>
    ) : CompositionSchema(definition = definition)

    companion object {
        /**
         * Determines the appropriate [OpenApiSchema.ContentSchema] based on the given composition
         * and a list of `Schema` objects.
         *
         * - If only one schema is present, it returns that schema directly.
         * - If multiple schemas are present, it combines them according to
         *   the specified `composition` type, defaulting to `Composition.ANY_OF`.
         *
         * @param composition The [Composition] type to apply when combining multiple schemas.
         *                    Defaults to `Composition.ANY_OF` if null.
         * @param schemas The list of [ElementSchema] objects to be combined. Assumes the list is non-empty and preprocessed.
         * @param examples The [IExample] object to associate with the schema.
         * @return An [OpenApiSchema.ContentSchema] representing the combined schema.
         */
        fun determine(
            composition: Composition?,
            schemas: List<ElementSchema>,
            examples: IExample?
        ): OpenApiSchema.ContentSchema {
            val combinedSchema: ISchemaFacet = when {
                schemas.size == 1 -> schemas.first()
                else -> when (composition ?: Composition.ANY_OF) {
                    Composition.ANY_OF -> AnyOf(anyOf = schemas)
                    Composition.ALL_OF -> AllOf(allOf = schemas)
                    Composition.ONE_OF -> OneOf(oneOf = schemas)
                }
            }

            return OpenApiSchema.ContentSchema(schema = combinedSchema, examples = examples)
        }
    }
}
