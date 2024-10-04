/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.request

import io.github.perracodex.kopapi.dsl.ApiMetadata
import io.github.perracodex.kopapi.dsl.ApiRequestBody
import io.github.perracodex.kopapi.dsl.ApiResponse
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * A builder for constructing a request body in an API endpoint's metadata.
 *
 * @property description A description of the request body's content and what it represents.
 * @property required Indicates whether the request body is mandatory for the API call.
 * @property contentType The [ContentType] of the request body data, such as JSON or XML.
 * @property deprecated Indicates if the request body is deprecated and should be avoided.
 *
 * @see [ApiMetadata.requestBody]
 */
public data class RequestBodyBuilder(
    var required: Boolean = true,
    var contentType: ContentType = ContentType.Application.Json,
    var deprecated: Boolean = false
) {
    var description: String by MultilineString()

    /**
     * Builds an [ApiResponse] instance from the current builder state.
     *
     * @param type The [KType] of the parameter.
     * @return The constructed [ApiResponse] instance.
     */
    @PublishedApi
    internal fun build(type: KType): ApiRequestBody {
        return ApiRequestBody(
            type = type,
            description = description.trimOrNull(),
            required = required,
            contentType = contentType,
            deprecated = deprecated
        )
    }
}
