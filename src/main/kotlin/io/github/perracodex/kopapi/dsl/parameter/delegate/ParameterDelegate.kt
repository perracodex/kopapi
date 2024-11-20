/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

// ParameterDelegate.kt
package io.github.perracodex.kopapi.dsl.parameter.delegate

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.element.ApiParameter
import io.github.perracodex.kopapi.dsl.parameter.builder.ParametersBuilder
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.sanitize

/**
 * Delegate for handling parameter registration.
 */
@KopapiDsl
internal class ParameterDelegate internal constructor(
    private val endpoint: String
) : IParameterConfigurable {
    /** Holds the parameters associated with the endpoint. */
    private val parameters: MutableSet<ApiParameter> = mutableSetOf()

    /**
     * Adds a collection of parameters defined within a `parameters { ... }` block.
     *
     * @param builder The builder used to configure the parameters.
     */
    override fun parameters(builder: ParametersBuilder.() -> Unit) {
        val parametersBuilder: ParametersBuilder = ParametersBuilder(endpoint = endpoint).apply(builder)
        parametersBuilder.build()?.forEach { addParameter(parameter = it) }
    }

    /**
     * Adds a new [ApiParameter] instance to the cache, ensuring that the parameter name is unique.
     *
     * @param parameter The [ApiParameter] instance to add.
     * @throws KopapiException If a parameter with the same name already exists.
     */
    @PublishedApi
    internal fun addParameter(parameter: ApiParameter) {
        val paramName: String = parameter.name.sanitize()

        if (paramName.isBlank()) {
            throw KopapiException("Parameter name must not be blank.")
        }

        if (parameters.any { it.name.equals(other = paramName, ignoreCase = true) && it.location == parameter.location }) {
            throw KopapiException(
                "Parameter with name '$paramName' and location '${parameter.location}' already exists within the same endpoint."
            )
        }

        parameters.add(parameter)
    }

    /**
     * Returns the registered parameters.
     */
    fun build(): MutableSet<ApiParameter>? = parameters.orNull()
}
