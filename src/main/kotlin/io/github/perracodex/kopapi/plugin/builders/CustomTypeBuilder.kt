/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.builders

import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.keys.DataFormat
import io.github.perracodex.kopapi.keys.DataType
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * A builder for constructing user-defined `custom types` to be used when generating the OpenAPI schema.
 *
 * ### Formats
 * The `format` field can be any custom text, or one of the standard OpenAPI formats.
 *
 * #### Sample Usage
 * ```
 * addType<Quote>(DataType.STRING) {
 *      maxLength = 256
 * }
 *
 * addType<DiscountRate>(DataType.NUMBER, "percentage") {
 *      minimum = 0,
 *      maximum = 100
 * }
 *
 * addType<Pin>(DataType.NUMBER, DataFormat.INT32) {
 *      minimum = 4,
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
public data class CustomTypeBuilder(
    var minLength: Int? = null,
    var maxLength: Int? = null,
    var minimum: Number? = null,
    var maximum: Number? = null,
    var exclusiveMinimum: Number? = null,
    var exclusiveMaximum: Number? = null,
    var multipleOf: Number? = null
) {
    /**
     * Builds an [CustomType] instance from the current builder state.
     *
     * @param type The [KType] of the parameter that specifies the target type.
     * @param dataType The [DataType] to be used in the OpenAPI schema.
     * @return The constructed [CustomType] instance.
     */
    @PublishedApi
    internal fun build(type: KType, dataType: DataType): CustomType {
        return build(type = type, dataType = dataType, format = null)
    }

    /**
     * Builds an [CustomType] instance from the current builder state.
     *
     * @param type The [KType] of the parameter that specifies the target type.
     * @param dataType The [DataType] to be used in the OpenAPI schema.
     * @param format The [DataFormat] to be used in the OpenAPI schema.
     * @return The constructed [CustomType] instance.
     */
    @PublishedApi
    internal fun build(type: KType, dataType: DataType, format: DataFormat): CustomType {
        return build(type = type, dataType = dataType, format = format.value)
    }

    /**
     * Builds an [CustomType] instance from the current builder state.
     *
     * @param type The [KType] of the parameter that specifies the target type.
     * @param dataType The [DataType] to be used in the OpenAPI schema.
     * @property format The format to be used in the OpenAPI schema, either standard or custom.
     * @return The constructed [CustomType] instance.
     */
    @PublishedApi
    internal fun build(type: KType, dataType: DataType, format: String?): CustomType {
        return CustomType(
            type = type,
            dataType = dataType,
            dataFormat = format.trimOrNull(),
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
