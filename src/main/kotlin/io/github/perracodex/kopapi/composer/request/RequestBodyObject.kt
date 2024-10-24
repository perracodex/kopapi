/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.ktor.http.*

/**
 * Represents the metadata for the request body of an API Operation.
 *
 * @property description A human-readable description of the parameter.
 * @property required Indicates whether the request body is mandatory.
 * @property content A map of [ContentType] to [OpenAPiSchema.ContentSchema] representing the content of the request body.
 */
@ComposerAPI
internal data class RequestBodyObject internal constructor(
    @JsonProperty("description") val description: String?,
    @JsonProperty("required") val required: Boolean,
    @JsonProperty("content") var content: Map<ContentType, OpenAPiSchema.ContentSchema>?
)
