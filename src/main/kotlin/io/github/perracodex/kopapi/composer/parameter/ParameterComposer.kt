/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.parameter

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.schema.facets.ElementSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.types.DefaultValue
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Responsible for composing the parameters section of the OpenAPI schema.
 *
 * @see [ParameterObject]
 * @see [ApiParameter]
 */
@ComposerApi
internal object ParameterComposer {
    private val tracer = Tracer<ParameterComposer>()

    /**
     * Generates the `parameters` section of the OpenAPI schema.
     *
     * @param apiParameters A set of [ApiParameter] instances to be composed into OpenAPI parameters.
     * @return A set of [ParameterObject] instances representing the OpenAPI parameters.
     */
    fun compose(
        apiParameters: Set<ApiParameter>,
    ): Set<ParameterObject> {
        tracer.info("Composing the 'parameters' section of the OpenAPI schema.")

        val parameterObjects: MutableSet<ParameterObject> = mutableSetOf()

        apiParameters.forEach { parameter ->
            tracer.debug("Composing parameter: ${parameter.name}")

            // Determine the schema for the parameter (complex or path type), and inspect accordingly.
            val baseSchema: ElementSchema = SchemaRegistry.inspectType(type = parameter.type)?.schema
                ?: throw KopapiException("No schema found for type: ${parameter.name}")

            // If a default value is present, create a copy of the schema with the default value.
            val schemaWithDefault: ElementSchema = parameter.defaultValue?.let { defaultValue ->
                tracer.debug("Found default value for parameter: ${parameter.name}")
                applyDefaultValue(baseSchema = baseSchema, defaultValue = defaultValue)
            } ?: baseSchema

            // Construct the parameter object.
            val parameterObject = ParameterObject(
                name = parameter.name,
                location = parameter.location,
                description = parameter.description.trimOrNull(),
                required = parameter.required,
                allowReserved = parameter.allowReserved,
                style = parameter.style?.value,
                explode = parameter.explode.takeIf { it == true },
                deprecated = parameter.deprecated.takeIf { it == true },
                schema = schemaWithDefault
            )
            parameterObjects.add(parameterObject)
        }

        tracer.debug("Composed ${parameterObjects.size} parameters.")

        return parameterObjects
    }

    /**
     * Applies the default value to the given schema by creating a copy of the specific schema subclass.
     *
     * @param baseSchema The original schema without the default value.
     * @param defaultValue The default value to apply to the schema.
     * @return A new [ElementSchema] instance with the default value set.
     * @throws KopapiException If the schema type is unsupported for applying a default value.
     */
    private fun applyDefaultValue(baseSchema: ElementSchema, defaultValue: DefaultValue): ElementSchema {
        tracer.debug("Applying default value to schema: ${baseSchema.definition}")
        return when (baseSchema) {
            is ElementSchema.AdditionalProperties -> baseSchema.copy(defaultValue = defaultValue.toValue())
            is ElementSchema.Array -> baseSchema.copy(defaultValue = defaultValue.toValue())
            is ElementSchema.Enum -> baseSchema.copy(defaultValue = defaultValue.toValue())
            is ElementSchema.Primitive -> baseSchema.copy(defaultValue = defaultValue.toValue())
            is ElementSchema.Reference -> baseSchema.copy(defaultValue = defaultValue.toValue())
            else -> baseSchema
        }
    }
}
