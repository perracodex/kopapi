/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.attributes

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.sanitize
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import java.util.*

/**
 * Builds a possible design-time link for a response.
 *
 * A Link Object represents a possible design-time link for a response, allowing
 * the API consumer to navigate to related operations.
 *
 * #### Attention
 * Either [operationId] or [operationRef] must be provided, but not both.
 *
 * @property operationId The name of an existing, resolvable OAS operation.
 * @property operationRef A reference to an existing operation using a URL or relative path.
 * @property description A human-readable description of the link.
 *
 * @see [ResponseBuilder]
 * @see [HeaderBuilder]
 */
@KopapiDsl
public class LinkBuilder internal constructor() {
    /**
     * The unique identifier of an existing operation in the OpenAPI specification.
     *
     * Must correspond to a valid, unique `operationId` defined in your API paths.
     *
     * **Note:** Either [operationId] or [operationRef] must be provided, but not both.
     */
    public var operationId: String? = null

    /**
     * A reference to an existing operation, defined using a URL or relative path.
     *
     * **Note:** Either [operationId] or [operationRef] must be provided, but not both.
     */
    public var operationRef: String? = null

    /** A brief description of the link's purpose.*/
    public var description: String by MultilineString()

    /**
     * A single expression or literal value to be used as the request body when calling the target operation.
     * This should represent the entire body payload, typically as a JSON string or an expression.
     */
    public var requestBody: String? = null

    /** A map representing parameters to pass to the linked operation. */
    private val parameters: SortedMap<String, String> = sortedMapOf()

    /**
     * Adds a parameter mapping from the current operation to the linked operation.
     *
     * @param name The name of the parameter in the linked operation.
     * @param value The value expression referencing the current request's data.
     *
     * #### Sample Usage
     * ```
     * link(name = "ErrorResponseLink") {
     *     operationId = "findEmployeeById"
     *     description = "The link to this error response."
     *     parameter(name = "employee_id", value = "$request.path.employee_id")
     * }
     * ```
     */
    public fun parameter(name: String, value: String) {
        val parameterName: String = name.sanitize()
        if (parameterName.isBlank()) {
            throw KopapiException("Parameter name must not be blank.")
        }
        parameters[name] = value.trimOrNull()
    }

    /**
     * Builds an [ApiLink] instance from the current builder state.
     *
     * @return The constructed [ApiLink] instance.
     * @throws KopapiException if neither [operationId] nor [operationRef] is provided, or if both are provided.
     */
    internal fun build(): ApiLink {
        when {
            operationId.isNullOrBlank() && operationRef.isNullOrBlank() ->
                throw KopapiException("Either `operationId` or `operationRef` must be provided.")

            !operationId.isNullOrBlank() && !operationRef.isNullOrBlank() ->
                throw KopapiException("Only one of `operationId` or `operationRef` should be provided.")
        }

        return ApiLink(
            operationId = operationId?.trimOrNull(),
            operationRef = operationRef?.trimOrNull(),
            description = description.trimOrNull(),
            parameters = parameters.takeIf { it.isNotEmpty() },
            requestBody = requestBody.trimOrNull()
        )
    }
}
