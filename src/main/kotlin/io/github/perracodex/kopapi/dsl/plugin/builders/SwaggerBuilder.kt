/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.types.SwaggerOperationsSorter
import io.github.perracodex.kopapi.types.SwaggerSyntaxTheme
import io.github.perracodex.kopapi.utils.NetworkUtils

/**
 * Constructs the Swagger configuration for the API documentation.
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
 *          withCredentials = true
 *          operationsSorter = SwaggerOperationsSorter.METHOD
 *          syntaxTheme = SwaggerSyntaxTheme.NORD
 *      }
 * }
 */
@KopapiDsl
public class SwaggerBuilder {
    /**
     * The URL to provide the `Swagger UI`.
     *
     * - Relative to the server root URL.
     * - Default: `/swagger-ui`.
     */
    public var url: String = DEFAULT_SWAGGER_URL

    /**
     * Whether to include cookies or other credentials in cross-origin (CORS) requests from Swagger UI.
     *
     * - Needed for APIs using session cookies in a cross-origin setup.
     * - Default: `false`.
     */
    public var withCredentials: Boolean = false

    /**
     * The sorter to use for the operations in the Swagger UI.
     *
     * - Default: [SwaggerOperationsSorter.UNSORTED].
     */
    public var operationsSorter: SwaggerOperationsSorter = SwaggerOperationsSorter.UNSORTED

    /**
     * The syntax highlighting theme to use for the Swagger UI.
     *
     * - Default: [SwaggerSyntaxTheme.AGATE].
     */
    public var syntaxTheme: SwaggerSyntaxTheme = SwaggerSyntaxTheme.AGATE

    /**
     * Produces an immutable [ApiDocs.Swagger] instance from the builder.
     */
    internal fun build(): ApiDocs.Swagger = ApiDocs.Swagger(
        url = NetworkUtils.normalizeUrl(url = url, defaultValue = DEFAULT_SWAGGER_URL),
        withCredentials = withCredentials,
        operationsSorter = operationsSorter,
        syntaxTheme = syntaxTheme
    )

    internal companion object {
        const val DEFAULT_SWAGGER_URL: String = "/swagger-ui"
    }
}