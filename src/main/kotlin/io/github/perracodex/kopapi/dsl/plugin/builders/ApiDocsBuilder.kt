/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.utils.NetworkUtils

/**
 * Constructs the information for the API documentation.
 */
@KopapiDsl
public class ApiDocsBuilder {
    /**
     * The URL to provide the OpenAPI schema in `JSON` format.
     *
     * - Relative to the server root URL.
     * - Default: `/openapi.json`.
     */
    public var openapiJsonUrl: String = DEFAULT_OPENAPI_JSON_URL

    /**
     * The URL to provide the OpenAPI schema in `YAML` format.
     *
     * - Relative to the server root URL.
     * - Default: `/openapi.yaml`.
     */
    public var openapiYamlUrl: String = DEFAULT_OPENAPI_YAML_URL

    /**
     * The URL to provide the OpenAPI `Redoc` documentation.
     *
     * - Relative to the server root URL.
     * - Default: `/redoc`.
     */
    public var redocUrl: String = DEFAULT_REDOC_URL

    /**
     * Holds the Swagger configuration.
     */
    private var swagger: ApiDocs.Swagger? = null

    /**
     * Constructs the Swagger configuration.
     *
     * #### Sample usage
     * ```
     * apiDocs {
     *      openapiYamlUrl = "/openapi.yaml"
     *      openapiJsonUrl = "/openapi.json"
     *      redocUrl = "/redoc"
     *
     *      swagger {
     *          url = "/swagger"
     *          persistAuthorization = true
     *          withCredentials = true
     *          displayRequestDuration = true
     *          displayOperationId = true
     *          operationsSorter = SwaggerOperationsSorter.METHOD
     *          syntaxTheme = SwaggerSyntaxTheme.NORD
     *      }
     * }
     *
     * @param block The configuration for the Swagger UI.
     */
    public fun swagger(block: SwaggerBuilder.() -> Unit) {
        swagger = SwaggerBuilder().apply(block).build()
    }

    /**
     * Produces an immutable [ApiInfo] instance from the builder.
     */
    internal fun build(): ApiDocs = ApiDocs(
        openapiYamlUrl = NetworkUtils.normalizeUrl(url = openapiYamlUrl, defaultValue = DEFAULT_OPENAPI_YAML_URL),
        openapiJsonUrl = NetworkUtils.normalizeUrl(url = openapiJsonUrl, defaultValue = DEFAULT_OPENAPI_JSON_URL),
        redocUrl = NetworkUtils.normalizeUrl(url = redocUrl, defaultValue = DEFAULT_REDOC_URL),
        swagger = swagger ?: SwaggerBuilder().build()
    )

    internal companion object {
        const val DEFAULT_OPENAPI_JSON_URL: String = "/openapi.json"
        const val DEFAULT_OPENAPI_YAML_URL: String = "/openapi.yaml"
        const val DEFAULT_REDOC_URL: String = "/redoc"
    }
}
