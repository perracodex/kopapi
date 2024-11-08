/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.utils.NetworkUtils
import io.github.perracodex.kopapi.utils.removeSuffixIgnoreCase

/**
 * Constructs the information for the API documentation.
 */
@KopapiDsl
public class ApiDocsBuilder internal constructor() {
    /**
     * Relative URL path for the OpenAPI schema.
     *
     * - Default:
     *      - `/openapi.yaml`
     *      - `/openapi.json`
     *
     * #### Directory Path
     * - Will append `openapi.yaml` and `openapi.json`.
     *      - *Example:* `"/api/"`
     *      - *Results:* `"/api/openapi.yaml"`, `"/api/openapi.json"`
     *
     * #### File Path with Format Extension (`.yaml` or `.json`)
     * - Retains the provided file and adds the alternate format extension.
     *      - *Example:* `"/api/spec.yaml"`
     *      - *Results:* `"/api/spec.yaml"`, `"/api/spec.json"`
     */
    public var openapiUrl: String = ""

    /**
     * The URL to provide the OpenAPI `Redoc` documentation.
     *
     * - Relative to the server root URL.
     * - Default: `/redoc`.
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
     *      openapiUrl = "/api/"
     *      redocUrl = "/api/redoc"
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
        val normalizedOpenapiUrl: String = NetworkUtils.normalizeUrl(
            url = openapiUrl,
            defaultValue = DEFAULT_OPENAPI_URL
        )

        val openapiYamlUrl: String
        val openapiJsonUrl: String

        when {
            normalizedOpenapiUrl.endsWith(suffix = JSON_EXTENSION, ignoreCase = true) -> {
                openapiJsonUrl = normalizedOpenapiUrl
                openapiYamlUrl = normalizedOpenapiUrl.removeSuffixIgnoreCase(JSON_EXTENSION).trim() + YAML_EXTENSION
            }
            normalizedOpenapiUrl.endsWith(suffix = YAML_EXTENSION, ignoreCase = true) -> {
                openapiYamlUrl = normalizedOpenapiUrl
                openapiJsonUrl = normalizedOpenapiUrl.removeSuffixIgnoreCase(YAML_EXTENSION).trim() + JSON_EXTENSION
            }
            else -> {
                openapiJsonUrl = "${normalizedOpenapiUrl.removeSuffix(suffix ="/").trim()}/$DEFAULT_OPENAPI_FILENAME$JSON_EXTENSION"
                openapiYamlUrl = "${normalizedOpenapiUrl.removeSuffix(suffix ="/").trim()}/$DEFAULT_OPENAPI_FILENAME$YAML_EXTENSION"
            }
        }

        val normalizedRedocUrl: String = NetworkUtils.normalizeUrl(
            url = redocUrl,
            defaultValue = DEFAULT_REDOC_URL
        )

        return ApiDocs(
            openapiYamlUrl = openapiYamlUrl,
            openapiJsonUrl = openapiJsonUrl,
            redocUrl = normalizedRedocUrl,
            swagger = swagger ?: SwaggerBuilder().build()
        )
    }

    internal companion object {
        const val DEFAULT_OPENAPI_URL: String = "/"
        const val DEFAULT_REDOC_URL: String = "${DEFAULT_OPENAPI_URL}redoc"

        const val YAML_EXTENSION: String = ".yaml"
        const val JSON_EXTENSION: String = ".json"
        const val DEFAULT_OPENAPI_FILENAME: String = "openapi"
    }
}
