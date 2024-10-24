/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.parameter

import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.schema.ElementSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Responsible for composing the parameters section of the OpenAPI schema.
 *
 * @see [ParameterObject]
 * @see [ApiParameter]
 */
@ComposerAPI
internal object ParameterComposer {
    /**
     * Generates the `parameters` section of the OpenAPI schema.
     *
     * @param apiParameters A set of [ApiParameter] instances to be composed into OpenAPI parameters.
     * @return A set of [ParameterObject] instances representing the OpenAPI parameters.
     */
    fun compose(
        apiParameters: Set<ApiParameter>,
    ): Set<ParameterObject> {
        val parameterObjects: MutableSet<ParameterObject> = mutableSetOf()

        apiParameters.forEach { parameter ->
            val schema: ElementSchema = parameter.complexType?.let { complexType ->
                SchemaRegistry.inspectType(type = complexType)?.schema
                    ?: throw KopapiException("No schema found for type: $complexType")
            } ?: parameter.pathType?.let { pathType ->
                ElementSchema.Primitive(
                    schemaType = pathType.apiType,
                    format = pathType.apiFormat?.value
                )
            } ?: throw KopapiException("No schema found for parameter: ${parameter.name}")

            val parameterObject = ParameterObject(
                name = parameter.name,
                location = parameter.location,
                description = parameter.description.trimOrNull(),
                required = parameter.required,
                allowEmptyValue = parameter.allowEmptyValue,
                allowReserved = parameter.allowReserved,
                style = parameter.style?.value,
                explode = parameter.explode.takeIf { it == true },
                deprecated = parameter.deprecated.takeIf { it == true },
                schema = schema,
                defaultValue = parameter.defaultValue?.toValue()
            )

            parameterObjects.add(parameterObject)
        }

        return parameterObjects
    }
}
