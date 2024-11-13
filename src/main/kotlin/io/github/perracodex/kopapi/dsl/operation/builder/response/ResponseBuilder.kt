/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builder.response

import io.github.perracodex.kopapi.dsl.example.delegate.ExampleDelegate
import io.github.perracodex.kopapi.dsl.example.delegate.IExampleConfigurable
import io.github.perracodex.kopapi.dsl.header.delegate.HeaderDelegate
import io.github.perracodex.kopapi.dsl.header.delegate.IHeaderConfigurable
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builder.attribute.LinkBuilder
import io.github.perracodex.kopapi.dsl.operation.builder.attribute.LinksBuilder
import io.github.perracodex.kopapi.dsl.operation.builder.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.builder.type.TypeConfig
import io.github.perracodex.kopapi.dsl.operation.element.ApiLink
import io.github.perracodex.kopapi.dsl.operation.element.ApiResponse
import io.github.perracodex.kopapi.dsl.schema.delegate.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.schema.delegate.SchemaAttributeDelegate
import io.github.perracodex.kopapi.dsl.schema.element.ApiSchemaAttributes
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.type.Composition
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.sanitize
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull
import io.ktor.http.*
import kotlin.reflect.typeOf

/**
 * A builder for constructing a response in an API endpoint's metadata.
 *
 * @property description A description of the response content and what it represents.
 * @property contentType A set of [ContentType]s for the response. Default: `JSON`.
 * @property composition The composition of the response. Only meaningful if multiple types are provided.
 *
 * @see [ApiOperationBuilder.response]
 */
@KopapiDsl
public class ResponseBuilder @PublishedApi internal constructor(
    private val schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate(),
    private val examplesDelegate: ExampleDelegate = ExampleDelegate(),
    private val headerDelegate: HeaderDelegate = HeaderDelegate()
) : ISchemaAttributeConfigurable by schemaAttributeDelegate,
    IExampleConfigurable by examplesDelegate,
    IHeaderConfigurable by headerDelegate {

    public var description: String by MultilineString()
    public var contentType: Set<ContentType> = setOf(ContentType.Application.Json)
    public var composition: Composition? = null

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config()

    /**
     * Registers a new type.
     *
     * #### Usage
     * ```
     * // Register a type defaulting to JSON.
     * addType<SomeType>()
     * ```
     * ```
     * // Register another type to a specific content type.
     * addType<SomeType> {
     *      contentType = setOf(ContentType.Application.Xml)
     * }
     * ```
     * ```
     * // Register a type with multiple content types.
     * addType<SomeType> {
     *      contentType = setOf(
     *          ContentType.Application.Json,
     *          ContentType.Application.Xml
     *      )
     * }
     * ```
     *
     * @receiver [TypeConfig] Optional lambda for configuring the type. Default: `JSON`.
     *
     * @param T The type of the response.
     */
    public inline fun <reified T : Any> addType(noinline builder: TypeConfig.() -> Unit = {}) {
        if (T::class == Unit::class || T::class == Nothing::class || T::class == Any::class) {
            return
        }

        // Ensure there is always a default content type.
        if (contentType.isEmpty()) {
            contentType = setOf(ContentType.Application.Json)
        }

        // Determine the effective content types for new type being added.
        val typeConfig: TypeConfig = TypeConfig().apply(builder)
        val effectiveContentTypes: Set<ContentType> = when {
            typeConfig.contentType.isNullOrEmpty() -> contentType
            else -> typeConfig.contentType ?: contentType
        }

        // Register the new type with the effective content types.
        val typeDetails: ApiResponse.TypeDetails = ApiResponse.TypeDetails(
            type = typeOf<T>(),
            schemaAttributes = typeConfig._schemaAttributes
        )
        effectiveContentTypes.forEach { contentTypeKey ->
            _config.allTypes.getOrPut(contentTypeKey) { mutableSetOf() }.add(typeDetails)
        }
    }

    /**
     * Adds a link to the response.
     *
     * #### Usage
     * ```
     * link(name = "GetEmployeeDetails") {
     *      operationId = "getEmployeeDetails"
     *      description = "Retrieve information about this employee."
     *      parameter(
     *          name = "employee_id",
     *          value = "\$request.path.employee_id"
     *      )
     * }
     * ```
     * ```
     * link(name = "UpdateEmployeeStatus") {
     *      operationId = "updateEmployeeStatus"
     *      description = "Link to update the status of this employee."
     *      parameter(
     *          name = "employee_id",
     *          value = "\$request.path.employee_id"
     *      )
     *      parameter( name = "status", value = "active")
     *      requestBody = "{\"status\": \"active\"}"
     * }
     * ```
     *
     * @receiver [LinkBuilder] The builder used to configure the link.
     *
     * @param name The unique name of the link.
     */
    public fun link(name: String, builder: LinkBuilder.() -> Unit) {
        links { add(name = name, builder = builder) }
    }

    /**
     * Adds a collection of links defined within a `links { ... }` block.
     *
     * The `links` block serves only as organizational syntactic sugar.
     * Links can be defined directly without needing to use the `links` block.
     *
     * #### Usage
     * ```
     * links {
     *      add(name = "GetEmployeeDetails") {
     *          operationId = "getEmployeeDetails"
     *          description = "Retrieve information about this employee."
     *          parameter(
     *              name = "employee_id",
     *              value = "\$request.path.employee_id"
     *          )
     *      }
     *      add(name = "UpdateEmployeeStatus") {
     *          operationId = "updateEmployeeStatus"
     *          description = "Link to update the status of this employee."
     *          parameter(
     *              name = "employee_id",
     *              value = "\$request.path.employee_id"
     *          )
     *          parameter(name = "status", value = "active")
     *          requestBody = "{\"status\": \"active\"}"
     *      }
     *      add(name = "ListEmployeeBenefits") {
     *          operationRef = "/api/v1/benefits/list"
     *          description = "List all benefits available to the employee."
     *          parameter(
     *              name = "employee_id",
     *              value = "\$request.path.employee_id"
     *          )
     *      }
     * }
     * ```
     *
     * @receiver [LinkBuilder] The builder used to configure the links.
     */
    public fun links(builder: LinksBuilder.() -> Unit) {
        val linksBuilder: LinksBuilder = LinksBuilder().apply(builder)
        linksBuilder.build()?.forEach { _config.addLink(name = it.key, link = it.value) }
    }

    @PublishedApi
    internal fun build(status: HttpStatusCode): ApiResponse {
        // Create the map of ContentType to Set<KType>, ensuring each ContentType maps to its specific types.
        val contentMap: Map<ContentType, Set<ApiResponse.TypeDetails>>? = _config
            .allTypes.orNull()
            ?.mapValues { it.value.toSet() }
            ?.filterValues { it.isNotEmpty() }
            ?.toSortedMap(
                compareBy(
                    { it.contentType },
                    { it.contentSubtype }
                )
            )

        // Return the constructed ApiResponse instance.
        return ApiResponse(
            status = status,
            description = description.trimOrNull(),
            headers = headerDelegate.build(),
            composition = composition,
            content = contentMap,
            links = _config.links.orNull(),
            examples = examplesDelegate.build()
        )
    }

    @PublishedApi
    internal inner class Config {
        /** Holds the types associated with the response. */
        val allTypes: MutableMap<ContentType, MutableSet<ApiResponse.TypeDetails>> = mutableMapOf()

        /** Holds the links associated with the response. */
        val links: MutableMap<String, ApiLink> = mutableMapOf()

        /**
         * Adds a new [ApiLink] instance to the cache, ensuring that the link name is unique
         *
         * @param name The unique name of the link.
         * @param link The [ApiLink] instance to add to the cache.
         * @throws KopapiException If an [ApiLink] with the same name already exists.
         */
        fun addLink(name: String, link: ApiLink) {
            val linkName: String = name.sanitize()
            if (linkName.isBlank()) {
                throw KopapiException("Link name must not be blank.")
            }
            if (links.any { it.key.equals(other = linkName, ignoreCase = true) }) {
                throw KopapiException("Link with name '${linkName}' already exists within the same response.")
            }
            links[linkName] = link
        }

        /**
         * Returns the registered schema attributes.
         */
        fun schemaAttributes(): ApiSchemaAttributes? {
            return schemaAttributeDelegate.attributes
        }
    }
}
