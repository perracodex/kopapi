/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.parameter

import io.github.perracodex.kopapi.dsl.ApiMetadata
import io.github.perracodex.kopapi.dsl.ApiParameter
import io.github.perracodex.kopapi.dsl.types.ParameterStyle
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * A builder for constructing path parameters in an API endpoint's metadata.
 *
 * @property description A description of the parameter's purpose and usage.
 * @property required Indicates whether the parameter is mandatory for the API call.
 * @property defaultValue The default value for the parameter if one is not provided.
 * @property style The style in which the parameter is serialized in the URL.
 * @property deprecated Indicates if the parameter is deprecated and should be avoided.
 *
 * @see [ApiMetadata.pathParameter]
 */
public data class PathParameterBuilder(
    var required: Boolean = true,
    var defaultValue: Any? = null,
    var style: ParameterStyle = ParameterStyle.SIMPLE,
    var deprecated: Boolean = false
) {
    var description: String by MultilineString()

    /**
     * Builds an [ApiParameter] instance from the current builder state.
     *
     * @param name The name of the parameter as it appears in the URL path.
     * @param type The [KType] of the parameter.
     * @return The constructed [ApiParameter] instance.
     */
    @PublishedApi
    internal fun build(name: String, type: KType): ApiParameter {
        return ApiParameter(
            type = type,
            location = ApiParameter.Location.PATH,
            name = name.trim(),
            description = description.trimOrNull(),
            required = required,
            defaultValue = defaultValue,
            style = style,
            deprecated = deprecated
        )
    }
}