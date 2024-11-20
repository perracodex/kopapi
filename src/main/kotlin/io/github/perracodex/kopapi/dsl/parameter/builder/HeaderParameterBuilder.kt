/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.parameter.builder

import io.github.perracodex.kopapi.dsl.example.delegate.ExampleDelegate
import io.github.perracodex.kopapi.dsl.example.delegate.IExampleConfigurable
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.element.ApiParameter
import io.github.perracodex.kopapi.dsl.schema.delegate.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.schema.delegate.SchemaAttributeDelegate
import io.github.perracodex.kopapi.type.DefaultValue
import io.github.perracodex.kopapi.type.ParameterStyle
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull
import kotlin.reflect.KType

/**
 * Builds a header parameter for an API endpoint's metadata.
 *
 * @property description A description of the parameter's purpose and usage.
 * @property required Indicates whether the parameter is mandatory for the API call.
 * @property defaultValue Optional default value for the parameter.
 * @property style The [ParameterStyle] in which the parameter is serialized in the URL.
 * @property deprecated Indicates if the parameter is deprecated and should be avoided.
 *
 * @see [CookieParameterBuilder]
 * @see [PathParameterBuilder]
 * @see [QueryParameterBuilder]
 */
@KopapiDsl
public class HeaderParameterBuilder @PublishedApi internal constructor(
    private val schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate(),
    private val examplesDelegate: ExampleDelegate = ExampleDelegate()
) : ISchemaAttributeConfigurable by schemaAttributeDelegate,
    IExampleConfigurable by examplesDelegate {
    public var description: String by MultilineString()
    public var required: Boolean = false
    public var defaultValue: DefaultValue? = null
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
            location = ApiParameter.Location.HEADER,
            name = name.trim(),
            description = description.trimOrNull(),
            required = required,
            allowReserved = null,
            defaultValue = defaultValue,
            style = style.takeIf { it != ParameterStyle.SIMPLE },
            explode = null,
            deprecated = deprecated.takeIf { it },
            schemaAttributes = schemaAttributeDelegate.attributes,
            examples = examplesDelegate.build()
        )
    }
}
