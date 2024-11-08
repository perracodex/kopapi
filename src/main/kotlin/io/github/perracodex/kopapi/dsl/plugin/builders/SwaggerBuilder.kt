/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.types.SwaggerDocExpansion
import io.github.perracodex.kopapi.types.SwaggerOperationsSorter
import io.github.perracodex.kopapi.types.SwaggerSyntaxTheme
import io.github.perracodex.kopapi.types.SwaggerUiTheme
import io.github.perracodex.kopapi.utils.NetworkUtils

/**
 * Constructs the Swagger configuration for the API documentation.
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
 */
@KopapiDsl
public class SwaggerBuilder internal constructor() {
    /**
     * The URL to provide the `Swagger UI`.
     *
     * - Relative to the server root URL.
     * - Default: `/swagger-ui/`.
     */
    public var url: String = ""

    /**
     * Whether to persist entered authorizations in `Swagger UI` so they are retained on page refresh.
     *
     * When enabled, any entered authorization is stored in the browser's local storage
     * so that it is not lost when the `Swagger UI` page is refreshed.
     *
     * - Default: `false`.
     */
    public var persistAuthorization: Boolean = false

    /**
     * Whether to include cookies or other credentials in cross-origin (CORS) requests from Swagger UI.
     *
     * - Needed for APIs using session cookies in a cross-origin setup.
     * - Default: `false`.
     */
    public var withCredentials: Boolean = false

    /**
     * The default expansion for the documentation in the `Swagger UI`.
     *
     * - Default: [SwaggerDocExpansion.NONE].
     *
     * #### Attention
     * [SwaggerDocExpansion.FULL] is recommended only for very small APIs.
     * For larger APIs, using `FULL` may cause the page to appear frozen or unresponsive
     * for a few moments during loading, as it tries to render all endpoints and details simultaneously.
     */
    public var docExpansion: SwaggerDocExpansion = SwaggerDocExpansion.LIST

    /**
     * Whether to display the request duration in the `Swagger UI`.
     *
     * - Default: `false`.
     */
    public var displayRequestDuration: Boolean = false

    /**
     * Whether to display the endpoint `operationId` in the `Swagger UI`.
     *
     * - Default: `false`.
     */
    public var displayOperationId: Boolean = false

    /**
     * The sorter to use for the operations in the Swagger UI.
     *
     * - Default: [SwaggerOperationsSorter.UNSORTED].
     */
    public var operationsSorter: SwaggerOperationsSorter = SwaggerOperationsSorter.UNSORTED

    /**
     * The theme to use for the overall Swagger UI.
     *
     * - Default: [SwaggerUiTheme.LIGHT].
     */
    public var uiTheme: SwaggerUiTheme = SwaggerUiTheme.LIGHT

    /**
     * The syntax highlighting theme to use for the Swagger UI.
     *
     * - Default: [SwaggerSyntaxTheme.AGATE].
     */
    public var syntaxTheme: SwaggerSyntaxTheme = SwaggerSyntaxTheme.AGATE

    /**
     * Whether to include detected errors during the API documentation generation
     * into the description of the `info` section of the Swagger UI.
     *
     * These are errors that do not adhere to the OpenAPI specification.
     *
     * - Default: `false`.
     */
    public var includeErrors: Boolean = false

    /**
     * Produces an immutable [ApiDocs.Swagger] instance from the builder.
     */
    internal fun build(): ApiDocs.Swagger = ApiDocs.Swagger(
        url = NetworkUtils.normalizeUrl(url = url, defaultValue = DEFAULT_SWAGGER_URL),
        persistAuthorization = persistAuthorization,
        withCredentials = withCredentials,
        docExpansion = docExpansion,
        displayRequestDuration = displayRequestDuration,
        displayOperationId = displayOperationId,
        operationsSorter = operationsSorter,
        uiTheme = uiTheme,
        syntaxTheme = syntaxTheme,
        includeErrors = includeErrors
    )

    internal companion object {
        const val DEFAULT_SWAGGER_URL: String = "/swagger-ui/"
    }
}
