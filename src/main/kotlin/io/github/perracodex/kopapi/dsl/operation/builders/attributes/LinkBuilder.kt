/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.attributes

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLinkParameter
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builds a possible design-time link for a response.
 *
 * @property operationId The name of an existing, resolvable OAS operation.
 * @property description A human-readable description of the link.
 *
 * @see [ResponseBuilder]
 * @see [HeaderBuilder]
 */
@KopapiDsl
public class LinkBuilder(
    public val operationId: String
) {
    public var description: String by MultilineString()

    /** A set representing parameters to pass to the linked operation. */
    private var parameters: LinkedHashSet<ApiLinkParameter> = linkedSetOf()

    init {
        if (operationId.isBlank()) {
            throw KopapiException("Link operation ID must not be empty.")
        }
    }

    /**
     * Adds a parameter to the link.
     *
     * #### Sample Usage
     * ```
     * link(operationId = "getEmployee") {
     *    description = "The employee to retrieve."
     *    parameter(name = "employeeId", value = "something")
     * }
     * ```
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     */
    public fun parameter(name: String, value: String) {
        val apiParameter = ApiLinkParameter(
            name = name.trim(),
            value = value.trim()
        )
        parameters.add(apiParameter)
    }

    /**
     * Builds an [ApiLink] instance from the current builder state.
     *
     * @return The constructed [ApiLink] instance.
     */
    internal fun build(): ApiLink {
        return ApiLink(
            operationId = operationId.trim(),
            description = description.trimOrNull(),
            parameters = parameters.takeIf { it.isNotEmpty() }
        )
    }
}
