/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core.dsl.builders.attributes

import io.github.perracodex.kopapi.core.dsl.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.core.dsl.elements.ApiLink
import io.github.perracodex.kopapi.core.dsl.elements.ApiLinkParameter
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
public class LinkBuilder(
    public val operationId: String
) {
    init {
        require(operationId.isNotBlank()) { "Operation ID must not be empty." }
    }

    public var description: String by MultilineString()

    /**
     * A set representing parameters to pass to the linked operation.
     */
    internal var parameters: LinkedHashSet<ApiLinkParameter> = linkedSetOf()

    /**
     * Adds a parameter to the link.
     *
     * #### Sample Usage
     * ```
     * link("getEmployee") {
     *    description = "The employee to retrieve."
     *    parameter("employeeId", "something")
     * }
     * ```
     *
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     */
    public fun LinkBuilder.parameter(
        name: String,
        value: String
    ) {
        parameters.add(ApiLinkParameter(name = name, value = value))
    }

    /**
     * Builds an [ApiLink] instance from the current builder state.
     *
     * @return The constructed [ApiLink] instance.
     */
    internal fun build(): ApiLink {
        return ApiLink(
            operationId = operationId,
            description = description.trimOrNull(),
            parameters = parameters.takeIf { it.isNotEmpty() }
        )
    }
}