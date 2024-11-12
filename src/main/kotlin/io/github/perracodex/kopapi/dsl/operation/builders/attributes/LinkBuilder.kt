/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.attributes

import io.github.perracodex.kopapi.dsl.headers.builders.HeadersBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.servers.builders.ServerConfigBuilder
import io.github.perracodex.kopapi.dsl.servers.builders.ServerVariableBuilder
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.sanitize
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import java.util.*

/**
 * Builds a possible design-time link for a response.
 *
 * A Link Object represents a possible design-time link for a response, allowing
 * the API consumer to navigate to related operations.
 *
 * #### Attention
 * Either [operationId] or [operationRef] must be provided, but not both.
 *
 * @property operationId The name of an existing, resolvable OAS operation.
 * @property operationRef A reference to an existing operation using a URL or relative path.
 * @property description A human-readable description of the link.
 *
 * @see [ResponseBuilder]
 * @see [HeadersBuilder]
 */
@KopapiDsl
public class LinkBuilder internal constructor() {
    /** A map representing parameters to pass to the linked operation. */
    private val parameters: SortedMap<String, String> = sortedMapOf()

    /** Cached server configuration. */
    private var serverConfig: ApiServerConfig? = null

    /**
     * The unique identifier of an existing operation in the OpenAPI specification.
     *
     * Must correspond to a valid, unique `operationId` defined in your API paths.
     *
     * **Note:** Either [operationId] or [operationRef] must be provided, but not both.
     */
    public var operationId: String? = null

    /**
     * A reference to an existing operation, defined using a URL or relative path.
     *
     * **Note:** Either [operationId] or [operationRef] must be provided, but not both.
     */
    public var operationRef: String? = null

    /** A brief description of the link's purpose.*/
    public var description: String by MultilineString()

    /**
     * A single expression or literal value to be used as the request body when calling the target operation.
     * This should represent the entire body payload, typically as a JSON string or an expression.
     */
    public var requestBody: String? = null

    /**
     * Adds a parameter mapping from the current operation to the linked operation.
     *
     * #### Usage
     * ```
     * link(name = "ErrorResponseLink") {
     *     operationId = "findEmployeeById"
     *     description = "The link to this error response."
     *     parameter(name = "employee_id", value = "$request.path.employee_id")
     * }
     * ```
     *
     * @param name The name of the parameter in the linked operation.
     * @param value The value expression referencing the current request's data.
     */
    public fun parameter(name: String, value: String) {
        val parameterName: String = name.sanitize()
        if (parameterName.isBlank()) {
            throw KopapiException("Parameter name must not be blank.")
        }
        parameters[name] = value.trimOrNull()
    }

    /**
     * Adds a server configuration with optional variables.
     *
     * #### Usage
     * ```
     * // Simple example with no variables.
     * server(urlString = "http://localhost:8080") {
     *      description = "Local server for development."
     * }
     * ```
     * ```
     * // Example with variable placeholders.
     * server(urlString = "{protocol}://{environment}.example.com:{port}") {
     *      description = "The server with environment variable."
     *
     *      // Environment.
     *      variable(name = "environment", defaultValue = "production") {
     *          choices = setOf("production", "staging", "development")
     *          description = "Specifies the environment (production, etc)"
     *      }
     *
     *      // Port.
     *      variable(name = "port", defaultValue = "8080") {
     *          choices = setOf("8080", "8443")
     *          description = "The port for the server."
     *      }
     *
     *      // Protocol.
     *      variable(name = "protocol", defaultValue = "http") {
     *          choices = setOf("http", "https")
     *      }
     * }
     * ```
     *
     * @receiver [ServerConfigBuilder] The builder used to configure a server.
     *
     * @param urlString The URL of the server. Expected to be a valid URL. If blank, the server is skipped.
     *
     * @see [ServerVariableBuilder]
     */
    public fun server(urlString: String, builder: ServerConfigBuilder.() -> Unit = {}) {
        if (urlString.isBlank()) {
            throw KopapiException("Server URL cannot be blank.")
        }
        serverConfig = ServerConfigBuilder(urlString = urlString).apply(builder).build()
    }

    /**
     * Builds an [ApiLink] instance from the current builder state.
     *
     * @return The constructed [ApiLink] instance.
     * @throws KopapiException if neither [operationId] nor [operationRef] is provided, or if both are provided.
     */
    internal fun build(): ApiLink {
        when {
            operationId.isNullOrBlank() && operationRef.isNullOrBlank() ->
                throw KopapiException("Either `operationId` or `operationRef` must be provided.")

            !operationId.isNullOrBlank() && !operationRef.isNullOrBlank() ->
                throw KopapiException("Only one of `operationId` or `operationRef` should be provided.")
        }

        return ApiLink(
            operationId = operationId?.trimOrNull(),
            operationRef = operationRef?.trimOrNull(),
            description = description.trimOrNull(),
            parameters = parameters.ifEmpty { null },
            requestBody = requestBody.trimOrNull(),
            server = serverConfig
        )
    }
}
