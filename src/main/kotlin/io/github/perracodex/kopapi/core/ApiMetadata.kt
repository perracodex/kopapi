/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.dsl.builders.ApiMetadataBuilder
import io.github.perracodex.kopapi.dsl.elements.ApiParameter
import io.github.perracodex.kopapi.dsl.elements.ApiRequestBody
import io.github.perracodex.kopapi.dsl.elements.ApiResponse
import io.github.perracodex.kopapi.dsl.elements.ApiSecurity
import io.ktor.http.*

/**
 * Represents the metadata of an API endpoint.
 *
 * This class is designed to be used in conjunction with Ktor routes,
 * enabling detailed descriptions of endpoint behaviors, parameters, responses,
 * and operational characteristics.
 *
 * @property path The URL path for the endpoint. Automatically derived from the Ktor route.
 * @property method The endpoint [HttpMethod] (GET, POST, etc.). Automatically derived from the Ktor route.
 * @property summary Optional short description of the endpoint's purpose.
 * @property description Optional detailed explanation of the endpoint and its functionality.
 * @property tags Optional set of descriptive labels for categorizing the endpoint in API documentation.
 * @property parameters Optional set of [ApiParameter] objects for defining endpoint parameters.
 * @property requestBody Optional [ApiRequestBody] object for defining the request body schema.
 * @property responses Optional set of [ApiResponse] objects for defining endpoint responses.
 * @property securitySchemes Optional set of [ApiSecurity] objects for defining endpoint security schemes.
 *
 * @see [ApiMetadataBuilder]
 */
internal data class ApiMetadata(
    val path: String,
    val method: HttpMethod,
    val summary: String?,
    val description: String?,
    val tags: Set<String>?,
    val parameters: Set<ApiParameter>?,
    val requestBody: ApiRequestBody?,
    val responses: Set<ApiResponse>?,
    val securitySchemes: Set<ApiSecurity>?
) {
    init {
        require(path.isNotBlank()) { "Endpoint path must not be empty." }
    }
}
