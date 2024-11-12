/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.operation

import io.github.perracodex.kopapi.dsl.headers.delegate.HeaderDelegate
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.request.RequestBodyBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.dsl.parameters.builders.ParametersBuilder
import io.github.perracodex.kopapi.dsl.parameters.delegate.IParameterConfigurable
import io.github.perracodex.kopapi.dsl.parameters.delegate.ParameterDelegate
import io.github.perracodex.kopapi.dsl.security.delegate.ISecurityConfigurable
import io.github.perracodex.kopapi.dsl.security.delegate.SecurityDelegate
import io.github.perracodex.kopapi.dsl.servers.delegate.IServerConfigurable
import io.github.perracodex.kopapi.dsl.servers.delegate.ServerDelegate
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.sanitize
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.string.SpacedString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import java.util.*

/**
 * Builder for constructing API Operations metadata for a route endpoint.
 *
 * #### Usage
 * ```
 * get("/items/{data_id}/{item_id?}") {
 *     // Implement as usual
 * } api {
 *     tags = setOf("Items", "Data")
 *     summary = "Retrieve data items."
 *     description = "Fetches all items for a data set."
 *     pathParameter<Uuid>("data_id") { description = "The data Id." }
 *     queryParameter<String>("item_id") { description = "Optional item Id." }
 *     response<List<Item>>(HttpStatusCode.OK) { description = "Successful." }
 *     response(HttpStatusCode.NotFound) { description = "Data not found." }
 * }
 * ```
 *
 * Usage involves defining a Ktor route and attaching API metadata using the `Route.api` infix
 * function to enrich the route with operational details and documentation specifications.
 *
 * #### Information
 * - [summary]: Optional short description of the endpoint's purpose.
 * - [description]: Optional detailed explanation of the endpoint and its functionality.
 * - [tags]: Optional set of descriptive tags for categorizing the endpoint in API documentation.
 *
 * #### Parameters
 * - [ParametersBuilder.pathParameter]: Adds a path parameter to the API endpoint's metadata.
 * - [ParametersBuilder.queryParameter]: Adds a query parameter to the API endpoint's metadata.
 * - [ParametersBuilder.headerParameter]: Adds a header parameter to the API endpoint's metadata.
 * - [ParametersBuilder.cookieParameter]: Adds a cookie parameter to the API endpoint's metadata.
 *
 * #### Request Body
 * - [requestBody]: Adds a request body to the API endpoint's metadata.
 *
 * #### Responses
 * - [response]: Add a response to the endpoint. Can be used with or without a response type.
 *  With no response type, the response is assumed to be only a [HttpStatusCode] with no associated type.
 *
 * #### Security Schemes
 * - [basicSecurity]: Adds a Basic security scheme to the API metadata.
 * - [bearerSecurity]: Adds a Bearer security scheme to the API metadata.
 * - [digestSecurity]: Adds a Digest security scheme to the API metadata.
 * - [headerApiKeySecurity]: Adds a Header API key security scheme to the API metadata.
 * - [queryApiKeySecurity]: Adds a Query API key security scheme to the API metadata.
 * - [cookieApiKeySecurity]: Adds a Cookie API key security scheme to the API metadata.
 * - [mutualTLSSecurity]: Adds a mutual TLS security scheme to the API metadata.
 * - [oauth2Security]: Adds an OAuth 2.0 security scheme to the API metadata.
 * - [openIdConnectSecurity]: Adds an OpenID Connect security scheme to the API metadata.
 * - [noSecurity]: Disables all security schemes for the API operation.
 */
@KopapiDsl
public class ApiOperationBuilder internal constructor(
    @PublishedApi internal val endpoint: String,
    private val serverDelegate: ServerDelegate = ServerDelegate(),
    private val securityDelegate: SecurityDelegate = SecurityDelegate(),
    private val parameterDelegate: ParameterDelegate = ParameterDelegate(endpoint = endpoint)
) : IServerConfigurable by serverDelegate,
    ISecurityConfigurable by securityDelegate,
    IParameterConfigurable by parameterDelegate {

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config()

    /**
     * Optional short description of the endpoint's purpose.
     *
     * #### Usage
     * ```
     * summary = "Retrieve data items."
     * ```
     *
     * Declaring the `summary` multiple times will concatenate all the summaries
     * delimited by a `space` character between each one.
     *
     * @see [description]
     * @see [tags]
     */
    public var summary: String by SpacedString()

    /**
     * Optional detailed explanation of the endpoint and its functionality.
     *
     * #### Usage
     * ```
     * description = "Fetches all items for a group."
     * ```
     *
     * Declaring the `description` multiple times will concatenate all the descriptions
     * delimited by a `newline` character between each one.
     *
     * @see [summary]
     * @see [tags]
     */
    public var description: String by MultilineString()

    /**
     * The identifier for the API operation.
     * Must be unique across all API operations.
     */
    public var operationId: String? = null

    /**
     * Optional set of descriptive tags for categorizing the endpoint in API documentation.
     *
     * #### Usage
     * ```
     * tags = setOf("Items", "Data")
     * ```
     * ```
     * tags("Items", "Data")
     * ```
     * ```
     * tags(listOf("Items", "Data"))
     * ```
     *
     * To include descriptions create top-level tags via the plugin configuration.
     *
     * @see [summary]
     * @see [description]
     */
    public var tags: Set<String>
        get() = _config.tags.toSet()
        set(value) {
            _config.tags.addAll(value.map { it.trim() }.filter { it.isNotBlank() })
        }

    /**
     * Optional set of descriptive tags for categorizing the endpoint in API documentation.
     *
     * #### Usage
     * ```
     * tags = setOf("Items", "Data")
     * ```
     * ```
     * tags("Items", "Data")
     * ```
     * ```
     * tags(listOf("Items", "Data"))
     * ```
     *
     * To include descriptions create top-level tags via the plugin configuration.
     *
     * @see [summary]
     * @see [description]
     */
    public fun tags(vararg tags: String) {
        this.tags = tags.toSet()
    }

    /**
     * Optional set of descriptive tags for categorizing the endpoint in API documentation.
     *
     * #### Usage
     * ```
     * tags = setOf("Items", "Data")
     * ```
     * ```
     * tags("Items", "Data")
     * ```
     * ```
     * tags(listOf("Items", "Data"))
     * ```
     *
     * To include descriptions create top-level tags via the plugin configuration.
     *
     * @see [summary]
     * @see [description]
     */
    public fun tags(tags: Collection<String>) {
        this.tags = tags.toSet()
    }

    /**
     * Disables the security schemes for the API operation.
     * Both top-level and operation-level security schemes will not be applied to this API Operation.
     *
     * @see [basicSecurity]
     * @see [bearerSecurity]
     * @see [digestSecurity]
     * @see [mutualTLSSecurity]
     * @see [oauth2Security]
     * @see [openIdConnectSecurity]
     */
    public fun noSecurity() {
        securityDelegate.clear()
        securityDelegate.noSecurity = true
    }

    /**
     * Registers a request body.
     *
     * #### Typed Request Body Example
     * ```
     * // Standard request body.
     * requestBody<MyRequestBodyType> {
     *      description = "The data required to create a new item."
     *      required = true
     *
     *      // Override the default JSON ContentType.
     *      contentType = setOf(
     *          ContentType.Application.Json,
     *          ContentType.Application.Xml
     *      )
     *
     *      // Optional composition.
     *      // Only meaningful if multiple types are provided.
     *      // If omitted, defaults to `AnyOf`.
     *      composition = Composition.AnyOf
     *
     *      // Register an additional type.
     *      addType<AnotherType>()
     *
     *      // Register another type but only set it to Xml.
     *      addType<YetAnotherType>(
     *          setOf(ContentType.Application.Xml)
     *      )
     * }
     * ```
     * #### Multipart Example
     * ```
     * requestBody<Unit> {
     *      // Implicit ContentType.MultiPart.FormData (default)
     *      multipart {
     *          part<PartData.FileItem>("file") {
     *              description = "The file to upload."
     *              contentType = setOf(
     *                  ContentType.Image.JPEG,
     *                  ContentType.Image.PNG
     *              )
     *          }
     *          part<PartData.FormItem>("metadata") {
     *              description = "Metadata about the file, provided as JSON."
     *          }
     *      }
     *
     *      // Explicit ContentType.MultiPart.Signed
     *      multipart {
     *          contentType = ContentType.MultiPart.Signed
     *
     *          part<PartData.FileItem>("secureFile") {
     *              description = "A securely uploaded file."
     *              contentType = setOf(
     *                  ContentType.Image.JPEG,
     *                  ContentType.Image.PNG
     *              )
     *          }
     *          part<PartData.FormItem>("metadata") {
     *              description = "Additional metadata about the file."
     *          }
     *      }
     * }
     * ```
     *
     * @receiver [RequestBodyBuilder] The builder used to configure the request body.
     *
     * @param T The body primary type of the request.
     *
     * @see [ParametersBuilder.cookieParameter]
     * @see [ParametersBuilder.headerParameter]
     * @see [ParametersBuilder.pathParameter]
     * @see [ParametersBuilder.queryParameter]
     * @see [response]
     */
    public inline fun <reified T : Any> requestBody(
        noinline builder: RequestBodyBuilder.() -> Unit = {}
    ) {
        if (_config.requestBody == null) {
            _config.requestBody = RequestBodyBuilder().apply {
                apply(builder)
                addType<T> {
                    this.contentType = this@apply.contentType
                    this._schemaAttributes = this@apply._config.schemaAttributes()
                }
            }.build()
        } else {
            val message: String = """
                |Only one RequestBody can be defined per API endpoint.
                |Attempted to define multiple RequestBodies within:
                |   '$endpoint'.
                """.trimMargin()
            throw KopapiException(message)
        }
    }

    /**
     * Registers a response.
     *
     * Responses can be registered with or without a response body type:
     * ```
     * response<ResponseType>(HttpStatusCode) { ... }
     * response(HttpStatusCode) { ... }
     * ```
     *
     * #### Usage
     *```
     * response<String>(status = HttpStatusCode.OK) {
     *      description = "Successfully retrieved the item."
     *
     *      // Override the default JSON ContentType.
     *      contentType = setOf(
     *          ContentType.Application.Json,
     *          ContentType.Application.Xml
     *      )
     *
     *      header<Int>(name = "X-Rate-Limit") {
     *          description = "Number of allowed requests per period."
     *          required = true
     *      }
     *      link(name = "SomeLinkName") {
     *          operationId = "someOperationId"
     *          description = "Some description."
     *          parameter(
     *              name = "some_id",
     *              value = "\$request.path.some_id"
     *          )
     *      }
     *
     *      // Only meaningful if multiple types are provided.
     *      // If omitted, defaults to `AnyOf`.
     *      composition = Composition.AnyOf
     *
     *      // Register additional type, but only set it to Xml.
     *      addType<YetAnotherType>(
     *          setOf(ContentType.Application.Xml)
     *      )
     * }
     *```
     *
     * @receiver [ResponseBuilder] The builder used to configure the response.
     *
     * @param status The [HttpStatusCode] code associated with this response.
     *
     * @see [HeaderDelegate.headers]
     * @see [ResponseBuilder.links]
     */
    @JvmName(name = "responseWithoutType")
    public fun response(
        status: HttpStatusCode,
        builder: ResponseBuilder.() -> Unit = {}
    ) {
        response<Unit>(status = status, builder = builder)
    }

    /**
     * Registers a response.
     *
     * Responses can be registered with or without a response body type:
     * ```
     * response<ResponseType>(HttpStatusCode) { ... }
     * response(HttpStatusCode) { ... }
     * ```
     *
     * #### Usage
     *```
     * response<String>(status = HttpStatusCode.OK) {
     *      description = "Successfully retrieved the item."
     *
     *      // Override the default JSON ContentType.
     *      contentType = setOf(
     *          ContentType.Application.Json,
     *          ContentType.Application.Xml
     *      )
     *
     *      header<Int>(name = "X-Rate-Limit") {
     *          description = "Number of allowed requests per period."
     *      }
     *      link(name = "SomeLinkName") {
     *          operationId = "someOperationId"
     *          description = "Some description."
     *          parameter(name = "some_id", value = "\$request.path.some_id")
     *      }
     *
     *      // Only meaningful if multiple types are provided.
     *      // If omitted, defaults to `AnyOf`.
     *      composition = Composition.AnyOf
     *
     *      // Register additional type, but only set it to Xml.
     *      addType<YetAnotherType> {
     *          contentType = setOf(ContentType.Application.Xml)
     *      }
     * }
     *```
     *
     * @receiver [ResponseBuilder] The builder used to configure the response.
     *
     * @param T The body primary type of the response.
     * @param status The [HttpStatusCode] code associated with this response.
     *
     * @see [HeaderDelegate.headers]
     * @see [ResponseBuilder.links]
     */
    @JvmName(name = "responseWithType")
    public inline fun <reified T : Any> response(
        status: HttpStatusCode,
        noinline builder: ResponseBuilder.() -> Unit = {}
    ) {
        val apiResponse: ApiResponse = ResponseBuilder().apply {
            apply(builder)
            addType<T> {
                this.contentType = this@apply.contentType
                this._schemaAttributes = this@apply._config.schemaAttributes()
            }
        }.build(status = status)

        _config.responses.merge(
            status,
            apiResponse
        ) { existing, new ->
            existing.mergeWith(other = new)
        }
    }

    /**
     * Constructs the API operation metadata for the route endpoint.
     *
     * @param method The [HttpMethod] associated with the route.
     * @param endpointPath The URL path for the route.
     * @return The constructed [ApiOperation] instance.
     */
    internal fun build(method: HttpMethod, endpointPath: String): ApiOperation {
        // If no responses are defined, add a default NoContent response,
        // otherwise sort the responses by status code.
        val responses: LinkedHashMap<HttpStatusCode, ApiResponse> = if (_config.responses.isEmpty()) {
            linkedMapOf(HttpStatusCode.NoContent to ApiResponse.buildWithNoContent())
        } else {
            _config.responses.entries
                .sortedBy { it.key.value }
                .associateTo(LinkedHashMap()) { it.toPair() }
        }

        return ApiOperation(
            path = endpointPath,
            method = method,
            tags = _config.tags.ifEmpty { null },
            summary = summary.trimOrNull(),
            description = description.trimOrNull(),
            operationId = operationId.trimOrNull()?.sanitize(),
            parameters = parameterDelegate.build(),
            requestBody = _config.requestBody,
            responses = responses,
            securitySchemes = securityDelegate.build(),
            noSecurity = securityDelegate.noSecurity,
            servers = serverDelegate.build()
        )
    }

    @PublishedApi
    internal class Config {
        /** Optional set of descriptive tags for categorizing the endpoint in API documentation. */
        val tags: SortedSet<String> = sortedSetOf(comparator = String.CASE_INSENSITIVE_ORDER)

        /** Optional structure and type of the request body. */
        var requestBody: ApiRequestBody? = null

        /** Optional set of possible responses, outlining expected status codes and content types. */
        var responses: MutableMap<HttpStatusCode, ApiResponse> = mutableMapOf()
    }
}
