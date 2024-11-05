/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.elements

import io.github.perracodex.kopapi.types.SwaggerOperationsSorter
import io.github.perracodex.kopapi.types.SwaggerSyntaxTheme

/**
 * Represents the API documentation configuration.
 *
 * @property openapiYamlUrl The URL to the OpenAPI YAML file.
 * @property openapiJsonUrl The URL to the OpenAPI JSON file.
 * @property redocUrl The URL to the Redoc documentation.
 * @property swagger The swagger configuration.
 */
internal data class ApiDocs(
    val openapiYamlUrl: String,
    val openapiJsonUrl: String,
    val redocUrl: String,
    val swagger: Swagger
) {
    /**
     * Represents the Swagger configuration.
     *
     * @property url The URL to the Swagger documentation.
     * @property persistAuthorization Whether to persist entered authorizations in Swagger UI.
     * @property withCredentials Whether to include cookies or other credentials in cross-origin (CORS) requests from Swagger UI.
     * @property displayRequestDuration Whether to display the request duration in Swagger UI.
     * @property operationsSorter The sorting order for the Swagger operations.
     * @property syntaxTheme The syntax highlighting theme for Swagger UI.
     */
    internal data class Swagger(
        val url: String,
        val persistAuthorization: Boolean,
        val withCredentials: Boolean,
        val displayRequestDuration: Boolean,
        val operationsSorter: SwaggerOperationsSorter,
        val syntaxTheme: SwaggerSyntaxTheme,
    )
}
