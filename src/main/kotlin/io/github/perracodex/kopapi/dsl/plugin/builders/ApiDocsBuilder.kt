/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.types.SwaggerSyntaxTheme
import io.github.perracodex.kopapi.utils.NetworkUtils

/**
 * Constructs the information for the API documentation.
 *
 * #### Sample usage
 * ```
 * apiDocs {
 *      openapiYamlUrl = "/openapi.yaml"
 *      openapiJsonUrl = "/openapi.json"
 *      redocUrl = "/redoc"
 *      swaggerUrl = "/swagger"
 *      withCredentials = true
 *      syntaxTheme = SwaggerSyntaxTheme.NORD
 * }
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
     * The URL to provide the `Swagger UI`.
     *
     * - Relative to the server root URL.
     * - Default: `/swagger-ui`.
     */
    public var swaggerUrl: String = DEFAULT_SWAGGER_URL

    /**
     * Whether to include cookies or other credentials in cross-origin (CORS) requests from Swagger UI.
     *
     * - Needed for APIs using session cookies in a cross-origin setup.
     * - Default: `false`.
     */
    public var withCredentials: Boolean = false

    /**
     * The syntax highlighting theme to use for the Swagger UI.
     *
     * - Default: [SwaggerSyntaxTheme.AGATE].
     */
    public var swaggerSyntaxTheme: SwaggerSyntaxTheme = SwaggerSyntaxTheme.AGATE

    /**
     * Produces an immutable [ApiInfo] instance from the builder.
     */
    internal fun build(): ApiDocs = ApiDocs(
        openapiYamlUrl = NetworkUtils.normalizeUrl(url = openapiYamlUrl, defaultValue = DEFAULT_OPENAPI_YAML_URL),
        openapiJsonUrl = NetworkUtils.normalizeUrl(url = openapiJsonUrl, defaultValue = DEFAULT_OPENAPI_JSON_URL),
        redocUrl = NetworkUtils.normalizeUrl(url = redocUrl, defaultValue = DEFAULT_REDOC_URL),
        swaggerUrl = NetworkUtils.normalizeUrl(url = swaggerUrl, defaultValue = DEFAULT_SWAGGER_URL),
        withCredentials = withCredentials,
        syntaxTheme = swaggerSyntaxTheme
    )

    internal companion object {
        const val DEFAULT_OPENAPI_JSON_URL: String = "/openapi.json"
        const val DEFAULT_OPENAPI_YAML_URL: String = "/openapi.yaml"
        const val DEFAULT_REDOC_URL: String = "/redoc"
        const val DEFAULT_SWAGGER_URL: String = "/swagger-ui"
    }
}
