/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Represents the metadata of an API response.
 *
 * @property type The [KType] of the response, defining the Kotlin type of the data expected in the response body.
 * @property status The [HttpStatusCode] code associated with this response.
 * @property description A human-readable description of the response, providing context about what this response signifies.
 * @property content A map of [ContentType] to [ContentSchemaReference] for the content in the response.
 * @property headers A list of [ApiHeader] objects representing the headers that may be included in the response.
 * @property links A list of [ApiLink] objects representing possible links to other operations.
 *
 * @see [ApiOperationBuilder.response]
 * @see [ApiHeader]
 * @see [ApiLink]
 */
internal data class ApiResponse(
    @JsonIgnore
    val type: KType?,
    @JsonIgnore
    val status: HttpStatusCode,
    val description: String?,
    val content: Map<ContentType, ContentSchemaReference>?,
    val headers: Set<ApiHeader>?,
    val links: Set<ApiLink>?
)
