/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.elements

import io.github.perracodex.kopapi.dsl.builders.ApiMetadataBuilder
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Represents the metadata of an API response.
 *
 * @property type The [KType] of the response, defining the Kotlin type of the data expected in the response body.
 * @property status The [HttpStatusCode] representing the HTTP status code that this response corresponds to.
 * @property description A human-readable description of the response, providing context about what this response signifies.
 * @property contentType The [ContentType] of the response, indicating the format in which the data is provided.
 * @property headers A list of [ApiHeader] objects representing the headers that may be included in the response.
 * @property links A list of [ApiLink] objects representing possible links to other operations.
 *
 * @see [ApiMetadataBuilder.response]
 * @see [ApiHeader]
 * @see [ApiLink]
 */
internal data class ApiResponse(
    val type: KType,
    val status: HttpStatusCode,
    val description: String? = null,
    val contentType: ContentType = ContentType.Application.Json,
    val headers: Set<ApiHeader>? = null,
    val links: Set<ApiLink>? = null
)
