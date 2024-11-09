/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.elements

import io.github.perracodex.kopapi.types.*

/**
 * Represents the API documentation configuration.
 *
 * @property openApiUrl The URL to the OpenAPI output.
 * @property openApiFormat The format of the OpenAPI schema output.
 * @property redocUrl The URL to the Redoc documentation.
 * @property swagger The swagger configuration.
 */
internal data class ApiDocs(
    val openApiUrl: String,
    val openApiFormat: OpenApiFormat,
    val redocUrl: String,
    val swagger: Swagger
) {
    /**
     * Represents the Swagger configuration.
     *
     * @property url The URL to the Swagger documentation.
     * @property persistAuthorization Whether to persist entered authorizations in Swagger UI.
     * @property withCredentials Whether to include cookies or other credentials in cross-origin (CORS) requests from Swagger UI.
     * @property docExpansion The default expansion state for the Swagger documentation.
     * @property displayRequestDuration Whether to display the request duration in Swagger UI.
     * @property displayOperationId Whether to display the endpoint `operationId` in Swagger UI.
     * @property operationsSorter The sorting order for the Swagger operations.
     * @property uiTheme The theme for Swagger UI.
     * @property syntaxTheme The syntax highlighting theme for Swagger UI.
     * @property includeErrors Whether to include API documentation generation errors into description of the `info` section.
     */
    internal data class Swagger(
        val url: String,
        val persistAuthorization: Boolean,
        val withCredentials: Boolean,
        val docExpansion: SwaggerDocExpansion,
        val displayRequestDuration: Boolean,
        val displayOperationId: Boolean,
        val operationsSorter: SwaggerOperationsSorter,
        val uiTheme: SwaggerUiTheme,
        val syntaxTheme: SwaggerSyntaxTheme,
        val includeErrors: Boolean
    )
}
