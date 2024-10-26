/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.parameter

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.types.ParameterStyle
import io.github.perracodex.kopapi.types.PathType
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.utils.io.*

/**
 * Builds a path parameter for an API endpoint's metadata.
 *
 * @property description A description of the parameter's purpose and usage.
 * @property style The style in which the parameter is serialized in the URL.
 * @property deprecated Indicates if the parameter is deprecated and should be avoided.
 *
 * @see [ApiOperationBuilder.pathParameter]
 * @see [CookieParameterBuilder]
 * @see [HeaderParameterBuilder]
 * @see [QueryParameterBuilder]
 */
@KtorDsl
@OperationDsl
public class PathParameterBuilder(
    public var style: ParameterStyle = ParameterStyle.SIMPLE,
    public var deprecated: Boolean = false
) {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiParameter] instance from the current builder state.
     *
     * @param name The name of the parameter as it appears in the URL path.
     * @param pathType The [PathType] of the parameter.
     * @return The constructed [ApiParameter] instance.
     */
    @PublishedApi
    internal fun build(name: String, pathType: PathType?): ApiParameter {
        return ApiParameter(
            complexType = null,
            pathType = pathType,
            location = ApiParameter.Location.PATH,
            name = name.trim(),
            description = description.trimOrNull(),
            required = true,
            allowReserved = null,
            allowEmptyValue = null,
            defaultValue = null,
            style = style.takeIf { it != ParameterStyle.SIMPLE },
            explode = null,
            deprecated = deprecated.takeIf { it },
        )
    }
}
