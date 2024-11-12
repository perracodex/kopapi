/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.parameters.builders

import io.github.perracodex.kopapi.dsl.examples.delegate.ExampleDelegate
import io.github.perracodex.kopapi.dsl.examples.delegate.IExampleConfigurable
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.dsl.schema.delegate.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.schema.delegate.SchemaAttributeDelegate
import io.github.perracodex.kopapi.types.ParameterStyle
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * Builds a path parameter for an API endpoint's metadata.
 *
 * @property description A description of the parameter's purpose and usage.
 * @property style The style in which the parameter is serialized in the URL.
 * @property deprecated Indicates if the parameter is deprecated and should be avoided.
 *
 * @see [CookieParameterBuilder]
 * @see [HeaderParameterBuilder]
 * @see [QueryParameterBuilder]
 */
@KopapiDsl
public class PathParameterBuilder @PublishedApi internal constructor(
    private val schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate(),
    private val examplesDelegate: ExampleDelegate = ExampleDelegate()
) : ISchemaAttributeConfigurable by schemaAttributeDelegate,
    IExampleConfigurable by examplesDelegate {

    public var description: String by MultilineString()
    public var style: ParameterStyle = ParameterStyle.SIMPLE
    public var deprecated: Boolean = false

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
            required = true,
            allowReserved = null,
            defaultValue = null,
            style = style.takeIf { it != ParameterStyle.SIMPLE },
            explode = null,
            deprecated = deprecated.takeIf { it },
            schemaAttributes = schemaAttributeDelegate.attributes,
            examples = examplesDelegate.build()
        )
    }
}
