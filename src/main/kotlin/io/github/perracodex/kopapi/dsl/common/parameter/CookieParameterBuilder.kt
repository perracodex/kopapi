/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.parameter

import io.github.perracodex.kopapi.dsl.common.example.configurables.ExampleDelegate
import io.github.perracodex.kopapi.dsl.common.example.configurables.IExampleConfigurable
import io.github.perracodex.kopapi.dsl.common.schema.configurable.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.common.schema.configurable.SchemaAttributeDelegate
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.types.DefaultValue
import io.github.perracodex.kopapi.types.ParameterStyle
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * Builds a cookie parameter for an API endpoint's metadata.
 *
 * @property description A description of the parameter's purpose and usage.
 * @property required Indicates whether the parameter is mandatory for the API call.
 * @property defaultValue Optional default value for the parameter.
 * @property style The style in which the parameter is serialized in the URL.
 * @property explode Whether to send arrays and objects as separate parameters.
 * @property deprecated Indicates if the parameter is deprecated and should be avoided.
 *
 * @see [HeaderParameterBuilder]
 * @see [PathParameterBuilder]
 * @see [QueryParameterBuilder]
 */
@KopapiDsl
public class CookieParameterBuilder @PublishedApi internal constructor(
    private val schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate(),
    private val examplesDelegate: ExampleDelegate = ExampleDelegate()
) : ISchemaAttributeConfigurable by schemaAttributeDelegate,
    IExampleConfigurable by examplesDelegate {
    public var description: String by MultilineString()
    public var required: Boolean = false
    public var defaultValue: DefaultValue? = null
    public var style: ParameterStyle = ParameterStyle.FORM
    public var explode: Boolean = true
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
            location = ApiParameter.Location.COOKIE,
            name = name.trim(),
            description = description.trimOrNull(),
            required = required,
            allowReserved = null,
            defaultValue = defaultValue,
            style = style.takeIf { it != ParameterStyle.FORM },
            explode = explode.takeIf { !it },
            deprecated = deprecated.takeIf { it },
            schemaAttributes = schemaAttributeDelegate.attributes,
            examples = examplesDelegate.build()
        )
    }
}
