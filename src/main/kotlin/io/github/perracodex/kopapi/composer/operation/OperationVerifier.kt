/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.operation

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.system.KopapiException

@ComposerApi
internal object OperationVerifier {
    /**
     * Helper method for consistency checks in API Operation.
     *
     * @throws KopapiException if detected that any errors in the API Operation configuration.
     */
    fun assert(apiOperations: Set<ApiOperation>) {
        if (apiOperations.isNotEmpty()) {
            verifyOperationIdUniqueness(apiOperations = apiOperations)
        }
    }

    /**
     * Verifies that all operation IDs are unique across all operations.
     *
     * @param apiOperations The set of API operations to verify.
     */
    private fun verifyOperationIdUniqueness(apiOperations: Set<ApiOperation>) {
        val duplicateOperations: Map<String, List<ApiOperation>> = apiOperations.asSequence()
            .mapNotNull { operation ->
                operation.operationId?.let { operationId ->
                    operationId.lowercase() to operation
                }
            }
            .groupBy { it.first }
            .filterValues { it.size > 1 }
            .mapValues { it.value.map { (_, operation) -> operation } }

        if (duplicateOperations.isNotEmpty()) {
            val message: String = duplicateOperations.map { (operationId, operations) ->
                val endpoints: String = operations.joinToString("\n") { op ->
                    "   - [${op.method.value}] → '${op.path}'"
                }
                "API operationId '$operationId' is not unique. " +
                        "The OpenAPI specification requires every operationId to be unique throughout the entire API. " +
                        "Duplicate found in:\n$endpoints\n"
            }.joinToString(separator = "\n\n")

            throw KopapiException(message)
        }
    }
}
