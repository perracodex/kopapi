/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import io.github.perracodex.kopapi.utils.MultilineString

/**
 * Represents a possible design-time link for a response.
 *
 * @property operationId The name of an existing, resolvable OAS operation.
 * @property description A human-readable description of the link.
 *
 * @see [ApiResponse]
 * @see [ApiLinkParameter]
 */
public data class ApiLink(
    val operationId: String
) {
    init {
        require(operationId.isNotBlank()) { "Operation ID must not be empty." }
    }

    public var description: String by MultilineString()

    /**
     * A map representing parameters to pass to the linked operation.
     */
    internal var parameters: LinkedHashSet<ApiLinkParameter>? = null

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
    public fun ApiLink.parameter(
        name: String,
        value: String
    ) {
        val parameters: LinkedHashSet<ApiLinkParameter> = parameters
            ?: linkedSetOf<ApiLinkParameter>().also { parameters = it }
        parameters.add(ApiLinkParameter(name = name, value = value))
    }
}
