/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.types.OpenApiFormat
import io.github.perracodex.kopapi.utils.NetworkUtils

/**
 * Constructs the information for the API documentation.
 */
@KopapiDsl
public class ApiDocsBuilder internal constructor() {
    /**
     * The URL path for the OpenAPI schema.
     *
     * - Default: `/openapi.yaml`
     * - Relative to the server root URL.
     *
     * #### Attention:
     *  The `openApiUrl` value does not determine the output format.
     *  Only the `openApiFormat` property specifies the format.
     *
     *  For example, if `openApiUrl` is set to `/openapi.json`, but `openApiFormat` is set `YAML`,
     *  the output will still be in `YAML` format.
     *
     * @see [openApiFormat]
     */
    public var openApiUrl: String = DEFAULT_OPENAPI_URL

    /**
     * The format of the OpenAPI schema output.
     *
     * - Default: `YAML`.
     *
     * @see [openApiUrl]
     */
    public var openApiFormat: OpenApiFormat = OpenApiFormat.YAML

    /**
     * The URL to provide the OpenAPI `Redoc` documentation.
     *
     * - Default: `/redoc`.
     * - Relative to the server root URL.
     */
    public var redocUrl: String = ""

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
     *      openApiUrl = "/openapi.yaml"
     *      openApiFormat = OpenApiFormat.YAML
     *      redocUrl = "/redoc"
     *
     *      swagger {
     *          url = "/swagger-ui/"
     *          persistAuthorization = true
     *          withCredentials = true
     *          docExpansion = SwaggerDocExpansion.LIST
     *          displayRequestDuration = true
     *          displayOperationId = true
     *          operationsSorter = SwaggerOperationsSorter.METHOD
     *          uiTheme = SwaggerUITheme.DARK
     *          syntaxTheme = SwaggerSyntaxTheme.NORD
     *          includeErrors = true
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
    internal fun build(): ApiDocs {
        val defaultOpenapiUrl: String = when (openApiFormat) {
            OpenApiFormat.YAML -> "/$DEFAULT_OPENAPI_FILENAME$YAML_EXTENSION"
            OpenApiFormat.JSON -> "/$DEFAULT_OPENAPI_FILENAME$JSON_EXTENSION"
        }

        val normalizedOpenapiUrl: String = NetworkUtils.normalizeUrl(
            url = openApiUrl,
            defaultValue = defaultOpenapiUrl
        )

        val normalizedRedocUrl: String = NetworkUtils.normalizeUrl(
            url = redocUrl,
            defaultValue = DEFAULT_REDOC_URL
        )

        return ApiDocs(
            openApiUrl = normalizedOpenapiUrl,
            openApiFormat = openApiFormat,
            redocUrl = normalizedRedocUrl,
            swagger = swagger ?: SwaggerBuilder().build()
        )
    }

    internal companion object {
        const val DEFAULT_OPENAPI_FILENAME: String = "openapi"
        const val YAML_EXTENSION: String = ".yaml"
        const val JSON_EXTENSION: String = ".json"

        const val DEFAULT_OPENAPI_URL: String = "/$DEFAULT_OPENAPI_FILENAME$YAML_EXTENSION"
        const val DEFAULT_REDOC_URL: String = "/redoc"
    }
}
