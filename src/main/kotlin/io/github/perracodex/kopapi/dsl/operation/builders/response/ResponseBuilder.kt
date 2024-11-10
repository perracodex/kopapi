/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.response

import io.github.perracodex.kopapi.dsl.common.example.configurables.ExampleDelegate
import io.github.perracodex.kopapi.dsl.common.example.configurables.IExampleConfigurable
import io.github.perracodex.kopapi.dsl.common.header.HeaderBuilder
import io.github.perracodex.kopapi.dsl.common.schema.configurable.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.common.schema.configurable.SchemaAttributeDelegate
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.LinkBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.LinksBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.type.TypeConfig
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.sanitize
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
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
    public var contentType: Set<ContentType> = setOf(ContentType.Application.Json),
    public var composition: Composition? = null,

    @Suppress("PropertyName")
    @PublishedApi
    internal val _schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate(),
    private val examplesDelegate: ExampleDelegate = ExampleDelegate()
) : ISchemaAttributeConfigurable by _schemaAttributeDelegate,
    IExampleConfigurable by examplesDelegate,
    HeaderBuilder() {

    public var description: String by MultilineString()

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config()

    /**
     * Registers a new type.
     *
     * #### Sample Usage
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
     * @param T The type of the response.
     * @param configure An optional lambda for configuring the type. Default: `JSON`.
     */
    @Suppress("DuplicatedCode")
    public inline fun <reified T : Any> addType(noinline configure: TypeConfig.() -> Unit = {}) {
        if (T::class == Unit::class || T::class == Nothing::class || T::class == Any::class) {
            return
        }

        // Ensure there is always a default content type.
        if (contentType.isEmpty()) {
            contentType = setOf(ContentType.Application.Json)
        }

        // Determine the effective content types for new type being added.
        val typeConfig: TypeConfig = TypeConfig().apply(configure)
        val effectiveContentTypes: Set<ContentType> = when {
            typeConfig.contentType.isNullOrEmpty() -> contentType
            else -> typeConfig.contentType ?: contentType
        }

        // Register the new type with the effective content types.
        val typeDetails: ApiResponse.TypeDetails = ApiResponse.TypeDetails(
            type = typeOf<T>(),
            schemaAttributes = typeConfig._schemaAttributeDelegate.attributes
        )
        effectiveContentTypes.forEach { contentTypeKey ->
            _config.allTypes.getOrPut(contentTypeKey) { mutableSetOf() }.add(typeDetails)
        }
    }

    /**
     * Adds a link to the response.
     *
     * #### Sample Usage
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
     * @param name The unique name of the link.
     * @param configure A lambda receiver for configuring the [LinkBuilder].
     */
    public fun link(name: String, configure: LinkBuilder.() -> Unit) {
        links { add(name = name, configure = configure) }
    }

    /**
     * Adds a collection of links defined within a `links { ... }` block.
     *
     * The `links` block serves only as organizational syntactic sugar.
     * Links can be defined directly without needing to use the `links` block.
     *
     * #### Sample Usage
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
     *
     * @param configure A lambda receiver for configuring the [LinksBuilder].
     */
    public fun links(configure: LinksBuilder.() -> Unit) {
        val linksBuilder: LinksBuilder = LinksBuilder().apply(configure)
        linksBuilder.build().forEach { _config.addLink(name = it.key, link = it.value) }
    }

    @PublishedApi
    internal fun build(status: HttpStatusCode): ApiResponse {
        // Create the map of ContentType to Set<KType>, ensuring each ContentType maps to its specific types.
        val contentMap: Map<ContentType, Set<ApiResponse.TypeDetails>>? = _config
            .allTypes.ifEmpty { null }
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
            headers = _headers.ifEmpty { null },
            composition = composition,
            content = contentMap,
            links = _config.links.ifEmpty { null },
            examples = examplesDelegate.build()
        )
    }

    @PublishedApi
    internal class Config {
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
    }
}
