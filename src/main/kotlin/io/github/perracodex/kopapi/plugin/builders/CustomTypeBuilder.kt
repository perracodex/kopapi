/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.builders

import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.keys.DataType
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * A builder for constructing user-defined `custom types` to be used when generating the OpenAPI schema.
 * These can be new unhandled types or existing standard types with custom specifications.
 *
 * ### Formats
 * The `format` field can be any custom text, or one of the standard OpenAPI formats:
 * ```
 * "byte", "date", "date-time", "double", "float", "int32", "int64", "time", "url", "uuid"
 * ```
 *
 * ### Additional Properties
 * The `additional` field can be used to add custom properties to the OpenAPI schema
 * that are not covered by the other fields. For example:
 *
 * #### Example
 * ```
 * addType<LanguageCode>(DataType.STRING) {
 *     format = "iso-code"
 *     minLength = 2
 *     maxLength = 2
 *     additional = mapOf(
 *          "code" to "EN",
 *          "description" to "ISO 639-1 language code"
 *     )
 * }
 * ```
 *
 * This will produce the following OpenAPI schema for the `Profile` class:
 * ```
 * "Profile": {
 *    "type": "object",
 *    "properties": {
 *       "id": {
 *          "type": "string",
 *          "format": "uuid"
 *       },
 *       "language": {
 *          "$ref": "#/components/schemas/CustomTypeOfLanguageCode"
 *       }
 *    }
 * }
 * ```
 *
 * In addition, the OpenAPI specification will include the schema for the `LanguageCode` type:
 * ```
 * "CustomTypeOfLanguageCode": {
 *    "type": "string",
 *    "format": "iso-code",
 *    "minLength": 2,
 *    "maxLength": 2,
 *    "code": "EN",
 *    "description": "ISO 639-1 language code"
 * }
 * ```
 *
 * @property format Used for defining expected data formats (e.g., "date", "email").
 * @property minLength Minimum length for string values.
 * @property maxLength Maximum length for string values.
 * @property minimum Minimum value for numeric types. Defines the inclusive lower bound.
 * @property maximum Maximum value for numeric types. Defines the inclusive upper bound.
 * @property exclusiveMinimum Exclusive lower bound for numeric types. The value is strictly greater.
 * @property exclusiveMaximum Exclusive upper bound for numeric types. The value is strictly less.
 * @property multipleOf Factor that constrains the value to be a multiple of a number.
 * @property additional Map for specifying any additional custom properties not covered by the above fields.
 *
 * @see [KopapiConfig.addType]
 */
public data class CustomTypeBuilder(
    var format: String? = null,
    var minLength: Int? = null,
    var maxLength: Int? = null,
    var minimum: Number? = null,
    var maximum: Number? = null,
    var exclusiveMinimum: Number? = null,
    var exclusiveMaximum: Number? = null,
    var multipleOf: Number? = null,
    var additional: Map<String, String>? = null
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
            multipleOf = multipleOf,
            additional = additional
        )
    }
}
