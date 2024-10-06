/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.builders

import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * A builder for constructing user defined `custom types` to be used when generating the OpenAPI schema.
 * These can be new unhandled types or existing standard types with custom specifications.
 *
 * @property format Optional format of the custom type.
 *
 * @see [KopapiConfig.customType]
 */
public data class CustomTypeBuilder(
    var format: String? = null
) {
    /**
     * Builds an [CustomType] instance from the current builder state.
     *
     * @param type The [KType] of the parameter that specifies the target type.
     * @param specType The type spec key. For example, `string`, `integer`, etc.
     * @return The constructed [CustomType] instance.
     */
    @PublishedApi
    internal fun build(type: KType, specType: String): CustomType {
        return CustomType(
            type = type,
            specType = specType.trim(),
            specFormat = format.trimOrNull()
        )
    }
}
