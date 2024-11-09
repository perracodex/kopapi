/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.operation

import io.github.perracodex.kopapi.dsl.common.parameter.configurable.ParametersBuilder
import io.github.perracodex.kopapi.dsl.common.security.configurable.ISecurityConfigurable
import io.github.perracodex.kopapi.dsl.common.security.configurable.SecurityConfigurable
import io.github.perracodex.kopapi.dsl.common.server.configurable.IServerConfigurable
import io.github.perracodex.kopapi.dsl.common.server.configurable.ServerConfigurable
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.HeaderBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.LinkBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.request.RequestBodyBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
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
 * Usage involves defining a Ktor route and attaching API metadata using the `Route.api` infix
 * function to enrich the route with operational details and documentation specifications.
 *
 * #### Information
 * - [summary]: Optional short description of the endpoint's purpose.
 * - [description]: Optional detailed explanation of the endpoint and its functionality.
 * - [tags]: Optional set of descriptive tags for categorizing the endpoint in API documentation.
 *
 * #### Parameters
 * - [pathParameter]: Adds a path parameter to the API endpoint's metadata.
 * - [queryParameter]: Adds a query parameter to the API endpoint's metadata.
 * - [headerParameter]: Adds a header parameter to the API endpoint's metadata.
 * - [cookieParameter]: Adds a cookie parameter to the API endpoint's metadata.
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
 * - [skipSecurity]: Disables all security schemes for the API operation.
 *
 * #### Sample Usage
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
 * @see [ApiOperation]
 */
@KopapiDsl
public class ApiOperationBuilder internal constructor(
    @PublishedApi internal val endpoint: String,
    private val serverConfigurable: ServerConfigurable = ServerConfigurable(),
    private val securityConfigurable: SecurityConfigurable = SecurityConfigurable()
) : IServerConfigurable by serverConfigurable,
    ISecurityConfigurable by securityConfigurable,
    ParametersBuilder(endpoint = endpoint) {

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config()

    /**
     * Optional short description of the endpoint's purpose.
     *
     * Declaring the `summary` multiple times will concatenate all the summaries
     * delimited by a `space` character between each one.
     *
     * #### Sample Usage
     * ```
     * summary = "Retrieve data items."
     * ```
     *
     * @see [description]
     * @see [tags]
     */
    public var summary: String by SpacedString()

    /**
     * Optional detailed explanation of the endpoint and its functionality.
     *
     * Declaring the `description` multiple times will concatenate all the descriptions
     * delimited by a `newline` character between each one.
     *
     * #### Sample Usage
     * ```
     * description = "Fetches all items for a group."
     * description = "In addition, it can fetch a specific item."
     * ```
     *
     * @see [summary]
     * @see [tags]
     */
    public var description: String by MultilineString()

    /**
     * Optional set of descriptive tags for categorizing the endpoint in API documentation.
     *
     * Declaring multiple `tags` will append all of them to the existing list.
     * Repeated tags are discarded in a case-insensitive manner.
     *
     * #### Sample Usage
     * ```
     * tags = setOf("Items", "Data")
     * tags = setOf("NewTag1", "NewTag2")
     * ```
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
     * The identifier for the API operation.
     * Must be unique across all API operations.
     */
    public var operationId: String? = null

    /**
     * Disables the security schemes for the API operation.
     * Both top-level and local-level security schemes will not be applied to this API Operation.
     *
     * @see [basicSecurity]
     * @see [bearerSecurity]
     * @see [digestSecurity]
     * @see [mutualTLSSecurity]
     * @see [oauth2Security]
     * @see [openIdConnectSecurity]
     */
    public fun skipSecurity() {
        securityConfigurable.securitySchemes.clear()
        securityConfigurable.skipSecurity = true
    }

    /**
     * Registers a request body.
     *
     * #### Sample Typed Request Body
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
     *
     * #### Sample Multipart
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
     *      // Explicit ContentType.MultiPart.Encrypted
     *      multipart {
     *          contentType = ContentType.MultiPart.Encrypted
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
     * @param T The body primary type of the request.
     * @param configure A lambda receiver for configuring the [RequestBodyBuilder].
     *
     * @see [RequestBodyBuilder]
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [pathParameter]
     * @see [queryParameter]
     * @see [response]
     */
    public inline fun <reified T : Any> requestBody(
        noinline configure: RequestBodyBuilder.() -> Unit = {}
    ) {
        if (_config.requestBody == null) {
            _config.requestBody = RequestBodyBuilder().apply {
                apply(configure)
                addType<T> {
                    this.contentType = this@apply.contentType
                    this._schemaAttributeConfigurable.attributes = this@apply._schemaAttributeConfigurable.attributes
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
     * #### Sample Usage
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
     * @param status The [HttpStatusCode] code associated with this response.
     * @param configure A lambda receiver for configuring the [ResponseBuilder].
     *
     * @see [ResponseBuilder]
     * @see [HeaderBuilder]
     * @see [LinkBuilder]
     */
    @JvmName(name = "responseWithoutType")
    public fun response(
        status: HttpStatusCode,
        configure: ResponseBuilder.() -> Unit = {}
    ) {
        response<Unit>(status = status, configure = configure)
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
     * #### Sample Usage
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
     * @param T The body primary type of the response.
     * @param status The [HttpStatusCode] code associated with this response.
     * @param configure A lambda receiver for configuring the [ResponseBuilder].
     *
     * @see [ResponseBuilder]
     * @see [HeaderBuilder]
     * @see [LinkBuilder]
     */
    @JvmName(name = "responseWithType")
    public inline fun <reified T : Any> response(
        status: HttpStatusCode,
        noinline configure: ResponseBuilder.() -> Unit = {}
    ) {
        val apiResponse: ApiResponse = ResponseBuilder().apply {
            apply(configure)
            addType<T> {
                this.contentType = this@apply.contentType
                this._schemaAttributeConfigurable.attributes = this@apply._schemaAttributeConfigurable.attributes
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
            tags = _config.tags.takeIf { it.isNotEmpty() },
            summary = summary.trimOrNull(),
            description = description.trimOrNull(),
            operationId = operationId.trimOrNull()?.sanitize(),
            parameters = _parametersConfig.parameters.takeIf { it.isNotEmpty() },
            requestBody = _config.requestBody,
            responses = responses,
            securitySchemes = securityConfigurable.securitySchemes.takeIf { it.isNotEmpty() },
            skipSecurity = securityConfigurable.skipSecurity,
            servers = serverConfigurable.servers.takeIf { it.isNotEmpty() }
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
