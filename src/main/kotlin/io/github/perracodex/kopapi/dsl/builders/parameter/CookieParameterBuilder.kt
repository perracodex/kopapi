/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.parameter

import io.github.perracodex.kopapi.dsl.builders.ApiMetadataBuilder
import io.github.perracodex.kopapi.dsl.elements.ApiParameter
import io.github.perracodex.kopapi.dsl.types.ParameterStyle
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * A builder for constructing cookie parameters in an API endpoint's metadata.
 *
 * @property description A description of the parameter's purpose and usage.
 * @property required Indicates whether the parameter is mandatory for the API call.
 * @property defaultValue The default value for the parameter if one is not provided.
 * @property explode Whether to send arrays and objects as separate parameters.
 * @property style The style in which the parameter is serialized in the URL.
 * @property deprecated Indicates if the parameter is deprecated and should be avoided.
 *
 * @see [ApiMetadataBuilder.cookieParameter]
 */
public data class CookieParameterBuilder(
    var required: Boolean = true,
    var defaultValue: Any? = null,
    var explode: Boolean = false,
    var style: ParameterStyle = ParameterStyle.FORM,
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
            location = ApiParameter.Location.COOKIE,
            name = name.trim(),
            description = description.trimOrNull(),
            required = required,
            defaultValue = defaultValue,
            explode = explode,
            style = style,
            deprecated = deprecated
        )
    }
}
