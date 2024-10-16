/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.path

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.composer.security.OperationSecurity
import io.github.perracodex.kopapi.composer.security.SecurityRequirement
import io.github.perracodex.kopapi.core.ApiOperation

/**
 * Responsible for composing the `paths` section of the OpenAPI schema.
 *
 * The `paths` section maps each API endpoint to its corresponding HTTP methods and associated
 * configurations, including parameters, request bodies, responses, and security requirements.
 *
 * @see [OpenAPiSchema.PathItem]
 */
@ComposerAPI
internal object PathComposer {
    /**
     * Generates the `paths` section of the OpenAPI schema by iterating over each API operation
     * and organizing them based on their HTTP method and path.
     *
     * This method ensures that each API operation is correctly placed under its respective path
     * and method, with all relevant configurations and security requirements applied.
     *
     * @param apiOperations A set of [ApiOperation] objects representing each API endpoint's metadata.
     * @param operationSecurity A list of [OperationSecurity] objects detailing the security
     *                          configurations for each API operation. This parameter can be `null`
     *                          if no operations require security.
     * @return A map where each key is an API Operation path and the value is an [OpenAPiSchema.PathItem]
     * object containing the HTTP methods and their configurations for that path.
     */
    fun compose(
        apiOperations: Set<ApiOperation>,
        operationSecurity: List<OperationSecurity>?
    ): Map<String, OpenAPiSchema.PathItem> {
        // Initialize a mutable map to hold the paths and their corresponding PathItem objects.
        val paths: MutableMap<String, OpenAPiSchema.PathItem> = mutableMapOf()

        // Iterate over each API Operation to organize them under their respective paths and methods.
        apiOperations.forEach { operation ->
            // Retrieve or create a PathItem for the operation's path.
            val pathItem: OpenAPiSchema.PathItem = paths.getOrPut(operation.path) {
                OpenAPiSchema.PathItem()
            }

            // Add the API Operation to the PathItem based on its HTTP method.
            pathItem.addOperation(
                method = operation.method,
                apiOperation = operation
            )

            // Locate the corresponding security configuration for the operation, if any.
            val securityConfig: List<SecurityRequirement>? = operationSecurity
                ?.find { operationSecurity ->
                    operationSecurity.method.equals(operation.method.value, ignoreCase = true) &&
                            operationSecurity.path.equals(operation.path, ignoreCase = true)
                }?.security

            // Assign the security configuration to the operation within the PathItem.
            pathItem.setSecurity(
                method = operation.method,
                security = securityConfig
            )
        }

        return paths
    }
}
