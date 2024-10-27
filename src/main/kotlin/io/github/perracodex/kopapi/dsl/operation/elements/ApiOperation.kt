/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.system.KopapiException
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
 * @property tags Optional set of descriptive labels for categorizing the endpoint in API documentation.
 * @property summary Optional short description of the endpoint's purpose.
 * @property description Optional detailed explanation of the endpoint and its functionality.
 * @property operationId A unique identifier for the operation, which must be unique across all operations.
 * @property parameters Optional set of [ApiParameter] objects for defining endpoint parameters.
 * @property requestBody Optional [ApiRequestBody] object for defining the request body schema.
 * @property responses Optional map of `status codes` to [ApiResponse] objects.
 * @property securitySchemes Optional set of [ApiSecurityScheme] objects for defining endpoint security schemes.
 * @property skipSecurity Whether no security is required and should ignore top level security schemes.
 *
 * @see [ApiOperationBuilder]
 */
internal data class ApiOperation(
    val path: String,
    val method: HttpMethod,
    val tags: Set<String>?,
    val summary: String?,
    val description: String?,
    val operationId: String?,
    val parameters: Set<ApiParameter>?,
    val requestBody: ApiRequestBody?,
    val responses: Map<HttpStatusCode, ApiResponse>?,
    val securitySchemes: Set<ApiSecurityScheme>?,
    val skipSecurity: Boolean
) {
    init {
        if (path.isBlank()) {
            throw KopapiException("Api Operation path must not be empty.")
        }

        if (responses.isNullOrEmpty()) {
            throw KopapiException("Api Operation must have at least one response defined.")
        }
    }
}
