/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.ktor.http.*

/**
 * Represents the metadata for the request body of an API Operation.
 *
 * @property description A human-readable description of the parameter.
 * @property required Indicates whether the request body is mandatory.
 * @property content A map of [ContentType] to [OpenApiSchema.ContentSchema] representing the content of the request body.
 */
@ComposerApi
internal data class RequestBodyObject internal constructor(
    @JsonProperty("description") val description: String?,
    @JsonProperty("required") val required: Boolean,
    @JsonProperty("content") var content: Map<ContentType, OpenApiSchema.ContentSchema>?
)
