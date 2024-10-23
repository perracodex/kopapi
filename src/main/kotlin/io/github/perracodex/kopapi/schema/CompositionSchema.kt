/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.utils.safeName

/**
 * Represents composition schemas that combine multiple element schemas based on composition rules.
 *
 * This sealed class defines schemas such as `anyOf`, `allOf`, and `oneOf` in OpenAPI.
 * These schemas allow combining or constraining the data using multiple schema rules.
 *
 * @property definition A unique identifier for debugging and clarity during schema generation.
 * @property ordinal Specifies the order of schema appearance when sorting.
 */
internal sealed class CompositionSchema(
    @JsonIgnore open val definition: String,
    @JsonIgnore open val ordinal: Int
) : IOpenApiSchema {
    /**
     * Represents a schema that allows for one or more schemas to be used interchangeably.
     *
     * @property anyOf A list of schemas, any of which can validate the data.
     */
    data class AnyOf(
        @JsonIgnore override val definition: String = AnyOf::class.safeName(),
        @JsonProperty("anyOf") val anyOf: List<ElementSchema>
    ) : CompositionSchema(definition = definition, ordinal = 1)

    /**
     * Represents a schema that requires all listed schemas to be validated against the data.
     *
     * @property allOf A list of schemas, all of which must validate the data.
     */
    data class AllOf(
        @JsonIgnore override val definition: String = AllOf::class.safeName(),
        @JsonProperty("allOf") val allOf: List<ElementSchema>
    ) : CompositionSchema(definition = definition, ordinal = 2)

    /**
     * Represents a schema where data must match exactly one of the listed schemas.
     *
     * @property oneOf A list of schemas, one of which must validate the data.
     */
    data class OneOf(
        @JsonIgnore override val definition: String = OneOf::class.safeName(),
        @JsonProperty("oneOf") val oneOf: List<ElementSchema>
    ) : CompositionSchema(definition = definition, ordinal = 3)
}
