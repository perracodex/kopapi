/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.parameter

import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.types.ParameterStyle
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * Builds a cookie parameter for an API endpoint's metadata.
 *
 * @property description A description of the parameter's purpose and usage.
 * @property required Indicates whether the parameter is mandatory for the API call.
 * @property defaultValue The default value for the parameter if one is not provided.
 * @property style The style in which the parameter is serialized in the URL.
 * @property explode Whether to send arrays and objects as separate parameters.
 * @property deprecated Indicates if the parameter is deprecated and should be avoided.
 *
 * @see [ApiOperationBuilder.cookieParameter]
 * @see [HeaderParameterBuilder]
 * @see [PathParameterBuilder]
 * @see [QueryParameterBuilder]
 */
public class CookieParameterBuilder(
    public var required: Boolean = true,
    public var defaultValue: Any? = null,
    public var style: ParameterStyle = ParameterStyle.FORM,
    public var explode: Boolean = true,
    public var deprecated: Boolean = false
) {
    public var description: String by MultilineString()

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
            style = style.takeIf { it != ParameterStyle.FORM },
            explode = explode.takeIf { !it },
            deprecated = deprecated.takeIf { it }
        )
    }
}
