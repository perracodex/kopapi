/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.safeName
import io.ktor.http.*

/**
 * Defines the schema types for multipart data used in `PartData` within Ktor's form-data requests.
 *
 * Multipart schemas represent various parts of form-data requests,
 * such as files, binary data, or form fields.
 *
 * @property definition A unique identifier for debugging and clarity during schema generation.
 * @property name The name of the multipart field or part in the request.
 * @property isRequired Indicates if the part is required for the request.
 */
@PublishedApi
internal sealed class MultipartSchema(
    @JsonIgnore open val definition: String,
    @JsonIgnore open val name: String,
    @JsonIgnore open val isRequired: Boolean,
) : ISchema {

    /**
     * Represents an object schema for multipart data with a set of named properties.
     *
     * @property properties A map of property names to their corresponding multipart schemas.
     */
    data class Object(
        @JsonIgnore override val definition: String = Object::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType = ApiType.OBJECT,
        @JsonProperty("description") val description: String?,
        @JsonProperty("properties") val properties: MutableMap<String, MultipartSchema> = mutableMapOf(),
        @JsonProperty("required") val requiredFields: List<String>?,
    ) : MultipartSchema(
        definition = definition,
        name = "",
        isRequired = true
    )

    /**
     * Represents a schema for files in multipart data (e.g., `PartData.FileItem`).
     *
     * @property name The name of the part in the form-data request, used as the property name.
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property format The format, which will be `binary` for file uploads.
     * @property description An optional description for this schema.
     * @property contentType The content type for the part.
     */
    data class FileItem(
        @JsonIgnore override val definition: String = FileItem::class.safeName(),
        @JsonIgnore override val name: String,
        @JsonIgnore override val isRequired: Boolean,
        @JsonIgnore val contentType: ContentType = ContentType.Application.OctetStream,
        @JsonIgnore val schemaType: ApiType = ApiType.STRING,
        @JsonIgnore val schemaFormat: ApiFormat = ApiFormat.BINARY,
        @JsonProperty("description") val description: String?
    ) : MultipartSchema(
        definition = definition,
        name = name,
        isRequired = isRequired
    ) {
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

    /**
     * Represents a schema for form items (e.g., `PartData.FormItem`).
     *
     * @property name The name of the part in the form-data request, used as the property name.
     * @property description An optional description for this schema.
     */
    data class FormItem(
        @JsonIgnore override val definition: String = FormItem::class.safeName(),
        @JsonIgnore override val name: String,
        @JsonIgnore override val isRequired: Boolean,
        @JsonIgnore val contentType: ContentType = ContentType.Text.Plain,
        @JsonIgnore val schemaType: ApiType = ApiType.STRING,
        @JsonIgnore val schemaFormat: ApiFormat? = null,
        @JsonProperty("description") val description: String?
    ) : MultipartSchema(
        definition = definition,
        name = name,
        isRequired = isRequired
    ) {
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

    /**
     * Represents a schema for binary items (e.g., `PartData.BinaryItem`).
     *
     * @property name The name of the part in the form-data request, used as the property name.
     * @property description An optional description for this schema.
     */
    data class BinaryItem(
        @JsonIgnore override val definition: String = BinaryItem::class.safeName(),
        @JsonIgnore override val name: String,
        @JsonIgnore override val isRequired: Boolean,
        @JsonIgnore val contentType: ContentType = ContentType.Application.OctetStream,
        @JsonIgnore val schemaType: ApiType = ApiType.STRING,
        @JsonIgnore val schemaFormat: ApiFormat = ApiFormat.BINARY,
        @JsonProperty("description") val description: String?
    ) : MultipartSchema(
        definition = definition,
        name = name,
        isRequired = isRequired
    ) {
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

    /**
     * Represents a schema for binary channel items (e.g., `PartData.BinaryChannelItem`).
     *
     * @property name The name of the part in the form-data request, used as the property name.
     * @property description An optional description for this schema.
     */
    data class BinaryChannelItem(
        @JsonIgnore override val definition: String = BinaryChannelItem::class.safeName(),
        @JsonIgnore override val name: String,
        @JsonIgnore override val isRequired: Boolean,
        @JsonIgnore val contentType: ContentType = ContentType.Application.OctetStream,
        @JsonIgnore val schemaType: ApiType = ApiType.STRING,
        @JsonIgnore val schemaFormat: ApiFormat = ApiFormat.BINARY,
        @JsonProperty("description") val description: String?
    ) : MultipartSchema(
        definition = definition,
        name = name,
        isRequired = isRequired
    ) {
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
