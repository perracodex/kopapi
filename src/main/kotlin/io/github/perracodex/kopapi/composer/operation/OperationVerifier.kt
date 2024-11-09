/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.operation

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation

@ComposerApi
internal object OperationVerifier {
    /**
     * Helper method for consistency checks in API Operation.
     *
     * @param newApiOperation The new API Operation to verify.
     * @param apiOperations The already registered API Operations to verify against.
     * @return A set of errors detected during the verification process, if any.
     */
    fun verify(newApiOperation: ApiOperation, apiOperations: Set<ApiOperation>): Set<String>? {
        return if (apiOperations.isNotEmpty()) {
            val errors: MutableSet<String> = mutableSetOf()

            assertUniqueApiOperation(newApiOperation = newApiOperation, apiOperations = apiOperations)?.let { error ->
                errors.add(error)
            }

            verifyOperationIdUniqueness(apiOperations = apiOperations)?.let {
                errors.add(it)
            }

            return errors.ifEmpty { null }
        } else {
            null
        }
    }

    /**
     * Verifies that the new API Operation is unique.
     *
     * @param newApiOperation The new API Operation to verify.
     * @param apiOperations The already registered API Operations to verify against.
     * @return An error message if the operation is not unique, otherwise null.
     */
    private fun assertUniqueApiOperation(newApiOperation: ApiOperation, apiOperations: Set<ApiOperation>): String? {
        val duplicateApiOperation: ApiOperation? = apiOperations.asSequence()
            .filter { operation ->
                operation.method == newApiOperation.method && operation.path == newApiOperation.path
            }.firstOrNull()

        return duplicateApiOperation?.let {
            return """
                |API operation path + method is not unique.
                |   - Due to shadowing only 1 operation is included in the OpenAPI schema.
                |   - [${newApiOperation.method.value}] → '${newApiOperation.path}'
                """.trimMargin()
        }
    }

    /**
     * Verifies that all operation IDs are unique across all operations.
     *
     * @param apiOperations The already registered API Operations to verify against.
     * @return An error message if any operation ID is not unique, otherwise null.
     */
    private fun verifyOperationIdUniqueness(apiOperations: Set<ApiOperation>): String? {
        val duplicateOperationsIds: Map<String, List<ApiOperation>> = apiOperations.asSequence()
            .mapNotNull { operation ->
                operation.operationId?.let { operationId ->
                    operationId.lowercase() to operation
                }
            }.groupBy { it.first }
            .filterValues { it.size > 1 }
            .mapValues { it.value.map { (_, operation) -> operation } }

        if (duplicateOperationsIds.isNotEmpty()) {
            return duplicateOperationsIds.map { (operationId, operations) ->
                val endpoints: String = operations.joinToString(separator = "\n") { operation ->
                    "       - [${operation.method.value}] → '${operation.path}'"
                }
                """
                |API operationId '$operationId' is not unique.
                |   - The OpenAPI specification requires every operationId to be unique throughout the entire API.
                |   - Duplicate found in:
                |$endpoints
                """.trimMargin()
            }.joinToString(separator = "\n\n")
        }

        return null
    }
}
