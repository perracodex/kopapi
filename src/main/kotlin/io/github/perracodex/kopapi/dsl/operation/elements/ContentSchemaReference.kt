/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import com.fasterxml.jackson.annotation.JsonProperty


/**
 * Represents the content of an API response, including the schema reference for the response body.
 *
 * @property schema The schema reference for the content, which points to an OpenAPI schema definition.
 */
internal data class ContentSchemaReference(
    val schema: SchemaReference
) {
    /**
     * Represents a reference to an OpenAPI schema component.
     *
     * @property ref The reference to a component schema (e.g., #/components/schemas/Employee).
     */
    internal data class SchemaReference(
        @JsonProperty("\$ref")
        var ref: String? = null,
    )
}
