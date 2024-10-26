/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.ConfigurationDsl
import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.utils.io.*
import kotlin.reflect.KType

/**
 * A builder for constructing user-defined `custom types` to be used when generating the OpenAPI schema.
 *
 * ### Formats
 * The `format` field can be any custom text, or one of the standard OpenAPI formats.
 *
 * #### Sample Usage
 * ```
 * addType<Quote>(apiType = ApiType.STRING) {
 *      maxLength = 256
 * }
 *
 * addType<DiscountRate>(apiType = ApiType.NUMBER, apiFormat = "percentage") {
 *      minimum = 0
 *      maximum = 100
 * }
 *
 * addType<Pin>(apiType = ApiType.NUMBER, apiFormat = ApiFormat.INT32) {
 *      minimum = 4
 *      maximum = 6
 * }
 * ```
 *
 * @property minLength Minimum length for string values.
 * @property maxLength Maximum length for string values.
 * @property minimum Minimum value for numeric types. Defines the inclusive lower bound.
 * @property maximum Maximum value for numeric types. Defines the inclusive upper bound.
 * @property exclusiveMinimum Exclusive lower bound for numeric types. The value is strictly greater.
 * @property exclusiveMaximum Exclusive upper bound for numeric types. The value is strictly less.
 * @property multipleOf Factor that constrains the value to be a multiple of a number.
 *
 * @see [KopapiConfig.addType]
 */
@KtorDsl
@ConfigurationDsl
public class CustomTypeBuilder {
    public var minLength: Int? = null
    public var maxLength: Int? = null
    public var minimum: Number? = null
    public var maximum: Number? = null
    public var exclusiveMinimum: Number? = null
    public var exclusiveMaximum: Number? = null
    public var multipleOf: Number? = null

    /**
     * Builds an [CustomType] instance from the current builder state.
     *
     * @param type The [KType] of the parameter that specifies the target type.
     * @param apiType The [ApiType] to be used in the OpenAPI schema.
     * @return The constructed [CustomType] instance.
     */
    @PublishedApi
    internal fun build(type: KType, apiType: ApiType): CustomType {
        return build(type = type, apiType = apiType, apiFormat = null)
    }

    /**
     * Builds an [CustomType] instance from the current builder state.
     *
     * @param type The [KType] of the parameter that specifies the target type.
     * @param apiType The [ApiType] to be used in the OpenAPI schema.
     * @param apiFormat The [ApiFormat] to be used in the OpenAPI schema.
     * @return The constructed [CustomType] instance.
     */
    @PublishedApi
    internal fun build(type: KType, apiType: ApiType, apiFormat: ApiFormat): CustomType {
        return build(type = type, apiType = apiType, apiFormat = apiFormat.value)
    }

    /**
     * Builds an [CustomType] instance from the current builder state.
     *
     * @param type The [KType] of the parameter that specifies the target type.
     * @param apiType The [ApiType] to be used in the OpenAPI schema.
     * @property apiFormat The format to be used in the OpenAPI schema, either standard or custom.
     * @return The constructed [CustomType] instance.
     */
    @PublishedApi
    internal fun build(type: KType, apiType: ApiType, apiFormat: String?): CustomType {
        return CustomType(
            type = type,
            apiType = apiType,
            apiFormat = apiFormat.trimOrNull(),
            minLength = minLength,
            maxLength = maxLength,
            minimum = minimum,
            maximum = maximum,
            exclusiveMinimum = exclusiveMinimum,
            exclusiveMaximum = exclusiveMaximum,
            multipleOf = multipleOf
        )
    }
}
