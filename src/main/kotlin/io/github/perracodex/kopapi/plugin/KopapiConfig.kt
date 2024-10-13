/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.inspector.custom.CustomTypeRegistry
import io.github.perracodex.kopapi.keys.ApiFormat
import io.github.perracodex.kopapi.keys.ApiType
import io.github.perracodex.kopapi.plugin.dsl.builders.CustomTypeBuilder
import io.github.perracodex.kopapi.plugin.dsl.builders.InfoBuilder
import io.github.perracodex.kopapi.plugin.dsl.builders.ServerBuilder
import io.github.perracodex.kopapi.plugin.dsl.elements.ApiInfo
import io.github.perracodex.kopapi.plugin.dsl.elements.ApiServerConfig
import kotlin.reflect.typeOf

/**
 * Configuration for the [Kopapi] plugin.
 */
public class KopapiConfig {
    /**
     * Whether the plugin should be enabled (Default is `true`).
     *
     * - When disabled, the plugin will not register any of the configuration URLs,
     *  and all the Routes API schema definitions will be discarded.
     *
     * - This is useful when you want to disable the plugin in certain environments, such as production.
     */
    public var enabled: Boolean = true

    /**
     * The URL to provide the OpenAPI schema in JSON format.
     * Relative to the server root URL. Default is `openapi/json`.
     */
    public var openapiJsonUrl: String = DEFAULT_OPENAPI_JSON_URL

    /**
     * The URL to provide the OpenAPI schema in YAML format.
     * Relative to the server root URL. Default is `openapi/yaml`.
     */
    public var openapiYamlUrl: String = DEFAULT_OPENAPI_YAML_URL

    /**
     * The URL to provide the Swagger UI.
     * Relative to the server root URL. Default is `swagger`.
     */
    public var swaggerUrl: String = DEFAULT_SWAGGER_URL

    /**
     * The URL to provide the raw pre-processed API metadata, for debugging purposes.
     * Relative to the server root URL. Default is `openapi/debug`.
     */
    public var debugUrl: String = DEFAULT_DEBUG_URL

    /**
     * The list of servers to include in the OpenAPI schema.
     */
    private val servers: MutableSet<ApiServerConfig> = mutableSetOf()

    /**
     * The [ApiInfo] metadata for the OpenAPI schema.
     */
    internal var apiInfo: ApiInfo? = null
        private set

    /**
     * Returns the ser of server configurations.
     * If no servers are added, a default server is returned.
     *
     * @return The set [ApiServerConfig] instances.
     */
    internal fun getServers(): Set<ApiServerConfig> {
        if (servers.isEmpty()) {
            return setOf(ServerBuilder.defaultServer())
        }
        return servers
    }

    /**
     * Sets up the OpenAPI metadata.
     *
     * #### Sample Usage
     * ```
     *  info {
     *      title = "API Title"
     *      description = "API Description"
     *      version = "1.0.0"
     *      termsOfService = "https://example.com/terms"
     *      contact {
     *          name = "API Support"
     *          url = "https://example.com/support"
     *          email = "example@email.com"
     *      }
     *      license {
     *          name = "MIT"
     *          url = "https://opensource.org/licenses/MIT"
     *      }
     *  }
     * ```
     *
     * @see [InfoBuilder]
     */
    public fun info(init: InfoBuilder.() -> Unit) {
        apiInfo = InfoBuilder().apply(init).build()
    }

    /**
     * Sets up servers, with optional support for variables.
     *
     * #### Sample Usage
     * ```
     * servers {
     *     add("http://localhost:8080") {
     *         description = "Local server for development."
     *     }
     *
     *     add("https://{environment}.example.com") {
     *         description = "The server for the API with environment variable."
     *         variable("environment") {
     *             description = "Specifies the environment (production, etc.)"
     *             defaultValue = "production"
     *             choices = setOf("production", "staging", "development")
     *         }
     *         variable("version") {
     *             description = "The version of the API."
     *             defaultValue = "v1"
     *             choices = setOf("v1", "v2")
     *         }
     *     }
     *
     *     add("https://{region}.api.example.com") {
     *         description = "Server for the API by region."
     *         variable("region") {
     *             description = "Specifies the region for the API (us, eu)."
     *             defaultValue = "us"
     *             choices = setOf("us", "eu")
     *         }
     *     }
     * }
     * ```
     *
     * @see [ServerBuilder]
     */
    public fun servers(init: ServerBuilder.() -> Unit) {
        servers.addAll(ServerBuilder().apply(init).build())
    }

    /**
     * Registers a new `custom type` to be used when generating the OpenAPI schema.
     *
     * #### Syntax
     * ```
     * addType<T>(ApiType, String) { configuration }
     * ```
     * Where `T` is the new Object class to register, `ApiType` is the type to be used in the OpenAPI schema,
     * and `format` is a free-text to define the expected api format.
     *
     * #### Sample
     * ```
     * addType<Quote>(ApiType.STRING) {
     *      maxLength = 256
     * }
     *
     * addType<DiscountRate>(ApiType.NUMBER, "percentage") {
     *      minimum = 0
     *      maximum = 100
     * }
     * ```
     *
     * @param T The new type to register. [Unit] and [Any] are not allowed.
     * @param type The [ApiType] to be used in the OpenAPI schema.
     * @param format Free-text to define expected api format, either standard or custom.
     * @param configure A lambda receiver to configure the [CustomTypeBuilder].
     *
     * @see [CustomTypeBuilder]
     */
    @OptIn(TypeInspectorAPI::class)
    public inline fun <reified T : Any> addType(
        type: ApiType,
        format: String? = null,
        configure: CustomTypeBuilder.() -> Unit = {}
    ) {
        val builder: CustomTypeBuilder = CustomTypeBuilder().apply(configure)
        val newCustomType: CustomType = builder.build(type = typeOf<T>(), apiType = type, apiFormat = format)
        CustomTypeRegistry.register(newCustomType)
    }

    /**
     * Registers a new `custom type` to be used when generating the OpenAPI schema.
     *
     * #### Syntax
     * ```
     * addType<T>(ApiType, ApiFormat) { configuration }
     * ```
     * Where `T` is the new Object class to register, `ApiType` is the type to be used in the OpenAPI schema,
     * and `format` is a field to define the expected api format.
     *
     * #### Sample
     * ```
     * addType<Pin>(ApiType.NUMBER, ApiFormat.INT32) {
     *      minimum = 4
     *      maximum = 6
     * }
     * ```
     *
     * @param T The new type to register. [Unit] and [Any] are not allowed.
     * @param type The [ApiType] to be used in the OpenAPI schema.
     * @param format The [ApiFormat] to be used in the OpenAPI schema.
     * @param configure A lambda receiver to configure the [CustomTypeBuilder].
     *
     * @see [CustomTypeBuilder]
     */
    @OptIn(TypeInspectorAPI::class)
    public inline fun <reified T : Any> addType(
        type: ApiType,
        format: ApiFormat,
        configure: CustomTypeBuilder.() -> Unit = {}
    ) {
        val builder: CustomTypeBuilder = CustomTypeBuilder().apply(configure)
        val newCustomType: CustomType = builder.build(type = typeOf<T>(), apiType = type, apiFormat = format)
        CustomTypeRegistry.register(newCustomType)
    }

    internal companion object {
        const val DEFAULT_OPENAPI_JSON_URL: String = "openapi/json"
        const val DEFAULT_OPENAPI_YAML_URL: String = "openapi/yaml"
        const val DEFAULT_SWAGGER_URL: String = "swagger"
        const val DEFAULT_DEBUG_URL: String = "openapi/debug"
    }
}
