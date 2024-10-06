/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.inspector.custom.CustomTypeRegistry
import io.github.perracodex.kopapi.plugin.builders.CustomTypeBuilder
import io.github.perracodex.kopapi.plugin.builders.Servers
import io.ktor.http.*
import kotlin.reflect.typeOf

/**
 * Configuration for the [Kopapi] plugin.
 */
public class KopapiConfig {
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
     * The URL to provide the API metadata, for debugging purposes.
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
     * These can be new unhandled types or existing standard types with custom specifications.
     *
     * #### Sample Usage
     * ```
     * customType<Test>("string")
     * customType<Measure>("number") { format = "float" }
     * customType<BigDecimal>("money") { format = "decimal" }
     * customType<Dog>("mammal") { format = "breed" }
     * ```
     *
     * @param T The new type to register. [Unit] and [Any] are not allowed.
     * @param configure A lambda receiver to configure the [CustomTypeBuilder].
     */
    public inline fun <reified T : Any> customType(type: String, configure: CustomTypeBuilder.() -> Unit = {}) {
        val builder: CustomTypeBuilder = CustomTypeBuilder().apply(configure)
        val newCustomType: CustomType = builder.build(type = typeOf<T>(), specType = type)
        CustomTypeRegistry.register(newCustomType)
    }
}
