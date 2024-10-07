/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
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
     * #### Syntax
     * ```
     * customType<T>("type") { configuration }
     * ```
     * Where `T` is the new Object class to register and `"type"` is the type name to be used in the OpenAPI schema.
     * The `"type"` name can be any custom text, or one of the standard OpenAPI types:
     * ```
     * "array", "boolean", "integer", "number", "object", "string"
     * ```
     *
     * #### Samples
     * ```
     * customType<Data>("string")
     * customType<Length>("number") { format = "float" }
     * customType<CurrencyCode>("string") { minLength = 3, maxLength = 3 }
     * ```
     *
     * #### Example
     * Let's say you want to register two new custom types: `CurrencyCode` and `DiscountRate`.
     *
     * - You wish to represent `CurrencyCode` as a `string` type with a specific length.
     * - You wish to represent `DiscountRate` as a `number` type with a percentage format.
     *
     * You can register these custom types as follows:
     * ```
     * customType<CurrencyCode>("string") {
     *     minLength = 3
     *     maxLength = 3
     * }
     *
     * customType<DiscountRate>("number") {
     *     format = "percentage"
     *     additional = mapOf("minimum" to "0", "maximum" to "100")
     * }
     * ```
     *
     * Now, if we have a class `Transaction` that uses both the `CurrencyCode` and `DiscountRate` types:
     * ```
     * data class Transaction(
     *     val id: Uuid,
     *     val currency: CurrencyCode,
     *     val discount: DiscountRate
     * )
     * ```
     *
     * This will produce the following OpenAPI schema for the `Transaction` class:
     * ```
     * "Transaction": {
     *    "type": "object",
     *    "properties": {
     *       "id": {
     *          "type": "string",
     *          "format": "uuid"
     *       },
     *       "currency": {
     *          "$ref": "#/components/schemas/CustomTypeOfCurrencyCode"
     *       },
     *       "discount": {
     *          "$ref": "#/components/schemas/CustomTypeOfDiscountRate"
     *       }
     *    }
     * }
     * ```
     *
     * In addition, the OpenAPI specification will include the schema for each custom type as follows:
     * ```
     * "CustomTypeOfCurrencyCode": {
     *    "type": "string",
     *    "minLength": 3,
     *    "maxLength": 3
     * }
     *
     * "CustomTypeOfDiscountRate": {
     *    "type": "number",
     *    "format": "percentage",
     *    "minimum": 0,
     *    "maximum": 100
     * }
     * ```
     *
     * @param T The new type to register. [Unit] and [Any] are not allowed.
     * @param type The type name to be used in the OpenAPI schema.
     * @param configure A lambda receiver to configure the [CustomTypeBuilder].
     *
     * @see [CustomTypeBuilder]
     */
    @OptIn(TypeInspectorAPI::class)
    public inline fun <reified T : Any> customType(type: String, configure: CustomTypeBuilder.() -> Unit = {}) {
        val builder: CustomTypeBuilder = CustomTypeBuilder().apply(configure)
        val newCustomType: CustomType = builder.build(type = typeOf<T>(), specType = type)
        CustomTypeRegistry.register(newCustomType)
    }
}
