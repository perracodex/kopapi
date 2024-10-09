/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.inspector.custom.CustomTypeRegistry
import io.github.perracodex.kopapi.keys.DataType
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
     * It can be a new unhandled type or an existing standard type with custom specifications.
     *
     * #### Syntax
     * ```
     * addType<T>(DataType) { configuration }
     * ```
     * Where `T` is the new Object class to register and `DataType` is the type to be used in the OpenAPI schema.
     *
     * #### Samples
     * ```
     * addType<Data>(DataType.STRING)
     * addType<Length>(DataType.NUMBER) { format = "float" }
     * addType<Password>(DataType.STRING) { minLength = 8, maxLength = 12 }
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
     * addType<CurrencyCode>(DataType.STRING) {
     *     minLength = 3
     *     maxLength = 3
     * }
     *
     * addType<DiscountRate>(DataType.NUMBER) {
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
     * @param type The [DataType] to be used in the OpenAPI schema.
     * @param configure A lambda receiver to configure the [CustomTypeBuilder].
     *
     * @see [CustomTypeBuilder]
     */
    @OptIn(TypeInspectorAPI::class)
    public inline fun <reified T : Any> addType(type: DataType, configure: CustomTypeBuilder.() -> Unit = {}) {
        val builder: CustomTypeBuilder = CustomTypeBuilder().apply(configure)
        val newCustomType: CustomType = builder.build(type = typeOf<T>(), dataType = type)
        CustomTypeRegistry.register(newCustomType)
    }
}
