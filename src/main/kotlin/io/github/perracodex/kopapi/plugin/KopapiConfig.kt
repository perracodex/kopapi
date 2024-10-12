/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.inspector.custom.CustomTypeRegistry
import io.github.perracodex.kopapi.keys.ApiFormat
import io.github.perracodex.kopapi.keys.ApiType
import io.github.perracodex.kopapi.plugin.builders.CustomTypeBuilder
import io.github.perracodex.kopapi.plugin.builders.Servers
import io.ktor.http.*
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
    public var openapiJsonUrl: String = "openapi/json"

    /**
     * The URL to provide the OpenAPI schema in YAML format.
     * Relative to the server root URL. Default is `openapi/yaml`.
     */
    public var openapiYamlUrl: String = "openapi/yaml"

    /**
     * The URL to provide the Swagger UI.
     * Relative to the server root URL. Default is `swagger`.
     */
    public var swaggerUrl: String = "swagger"

    /**
     * The URL to provide the raw pre-processed API metadata, for debugging purposes.
     * Relative to the server root URL. Default is `openapi/debug`.
     */
    public var debugUrl: String = "openapi/debug"

    /**
     * The list of servers to include in the OpenAPI schema.
     */
    internal val servers: Servers = Servers()

    /**
     * Appends a list of servers to the configuration.
     * Can be defined as strings or [Url] objects.
     *
     * #### Sample Usage
     * ```
     * servers {
     *      add("http://localhost:8080")
     *      add(Url("http://localhost:8081"))
     * }
     * ```
     */
    public fun servers(init: Servers.() -> Unit) {
        servers.init()
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
}
