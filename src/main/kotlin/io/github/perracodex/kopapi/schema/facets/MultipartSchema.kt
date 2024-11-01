/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema.facets

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.safeName
import io.ktor.http.*
import io.ktor.http.content.*

/**
 * Defines the schema types for multipart data used in [PartData] within Ktor's form-data requests.
 *
 * @property definition A unique identifier for debugging and clarity during schema generation.
 * @property isRequired Indicates if the part is required for the request.
 */
internal sealed class MultipartSchema(
    @JsonIgnore open val definition: String,
    @JsonIgnore open val isRequired: Boolean
) : ISchemaFacet {
    /**
     * Represents an object schema for multipart data with a set of named `part` properties.
     *
     * @property description An optional description for this schema.
     * @property schemaType The schema type of the object.
     * @property properties A map of property names to their corresponding multipart schemas.
     * @property requiredFields A list of required field names, if any.
     */
    data class Object(
        @JsonIgnore override val definition: String = Object::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType = ApiType.OBJECT,
        @JsonProperty("description") val description: String?,
        @JsonProperty("properties") val properties: MutableMap<String, MultipartSchema> = mutableMapOf(),
        @JsonProperty("required") val requiredFields: List<String>?,
    ) : MultipartSchema(
        definition = definition,
        isRequired = true
    )

    /**
     * Represents a single `part` of a multipart request.
     *
     * @property name The name of the multipart field or part in the request.
     * @property contentType The content type of the part.
     * @property schemaType The schema type of the part.
     * @property schemaFormat Optional schema format for the part.
     * @property description An optional description for this part.
     */
    data class PartItem(
        @JsonIgnore override val definition: String = PartItem::class.safeName(),
        @JsonIgnore override val isRequired: Boolean,
        @JsonIgnore val name: String,
        @JsonIgnore val contentType: ContentType,
        @JsonIgnore val schemaType: ApiType,
        @JsonIgnore val schemaFormat: ApiFormat?,
        @JsonProperty("description") val description: String?
    ) : MultipartSchema(
        definition = definition,
        isRequired = isRequired
    ) {
        /**
         * Constructs the OpenApi schema for the part item.
         */
        @JsonProperty("content")
        val content: Map<String, Map<String, Any?>> = mapOf(
            contentType.toString() to mapOf(
                "schema" to mapOf(
                    "type" to schemaType,
                    "format" to schemaFormat
                )
            )
        )
    }
}
