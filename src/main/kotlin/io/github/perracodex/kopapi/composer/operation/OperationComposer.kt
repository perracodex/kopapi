/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.operation

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.composer.parameter.ParameterComposer
import io.github.perracodex.kopapi.composer.parameter.ParameterObject
import io.github.perracodex.kopapi.composer.request.RequestBodyComposer
import io.github.perracodex.kopapi.composer.request.RequestBodyObject
import io.github.perracodex.kopapi.composer.response.ResponseComposer
import io.github.perracodex.kopapi.composer.response.ResponseObject
import io.github.perracodex.kopapi.composer.security.SecurityObject
import io.github.perracodex.kopapi.composer.security.SecurityRequirement
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation

/**
 * Responsible for composing the `paths` section of the OpenAPI schema.
 *
 * The `paths` section maps each API endpoint to its corresponding HTTP methods and associated
 * configurations, including parameters, request bodies, responses, and security requirements.
 *
 * @see [OpenAPiSchema.PathItemObject]
 */
@ComposerAPI
internal object OperationComposer {
    /**
     * Generates the `paths` section of the OpenAPI schema by iterating over each API operation
     * and organizing them based on their HTTP method and path.
     *
     * This method ensures that each API operation is correctly placed under its respective path
     * and method, with all relevant configurations and security requirements applied.
     *
     * @param apiOperations A set of [ApiOperation] objects representing each API endpoint's metadata.
     * @param securityObject A list of [SecurityObject] objects detailing the security
     *                       configurations for each API operation. This parameter can be `null`
     *                       if no operations require security.
     * @return A map where each key is an API Operation path and the value is an [OpenAPiSchema.PathItemObject]
     * object containing the HTTP methods and their configurations for that path.
     */
    fun compose(
        apiOperations: Set<ApiOperation>,
        securityObject: List<SecurityObject>?
    ): Map<String, OpenAPiSchema.PathItemObject> {

        val pathItems: MutableMap<String, OpenAPiSchema.PathItemObject> = mutableMapOf()

        apiOperations.forEach { operation ->
            // Retrieve or create the PathItemObject for the operation's path.
            val pathItemObject: OpenAPiSchema.PathItemObject = pathItems.getOrPut(operation.path) {
                OpenAPiSchema.PathItemObject()
            }

            // Transform the ApiOperation.
            val operationObject: OperationObject = fromApiOperation(apiOperation = operation)
            pathItemObject.addOperation(
                method = operation.method,
                operationObject = operationObject
            )

            // Locate the corresponding security configuration for the operation, if any.
            val securityConfig: List<SecurityRequirement>? = securityObject
                ?.find { operationSecurity ->
                    operationSecurity.method.equals(operation.method.value, ignoreCase = true) &&
                            operationSecurity.path.equals(operation.path, ignoreCase = true)
                }?.security

            // Assign the security configuration.
            pathItemObject.setSecurity(
                method = operation.method,
                security = securityConfig
            )
        }

        return pathItems
    }

    /**
     * Creates an [OperationObject] instance from an [ApiOperation] object.
     *
     * This factory method facilitates the transformation of an [ApiOperation] into
     * an [OperationObject] suitable for inclusion in the OpenAPI schema.
     *
     * @param apiOperation The [ApiOperation] object containing the operation's metadata.
     * @return An [OperationObject] instance populated with data from [apiOperation].
     */
    private fun fromApiOperation(apiOperation: ApiOperation): OperationObject {
        val parameters: Set<ParameterObject>? = apiOperation.parameters?.let {
            ParameterComposer.compose(apiParameters = apiOperation.parameters)
        }
        val requestBody: RequestBodyObject? = apiOperation.requestBody?.let {
            RequestBodyComposer.compose(requestBody = apiOperation.requestBody)
        }
        val responses: Map<String, ResponseObject>? = apiOperation.responses?.let {
            ResponseComposer.compose(responses = apiOperation.responses)
        }
        return OperationObject(
            summary = apiOperation.summary,
            description = apiOperation.description,
            tags = apiOperation.tags.takeIf { !it.isNullOrEmpty() },
            parameters = parameters.takeIf { !it.isNullOrEmpty() },
            requestBody = requestBody,
            responses = responses.takeIf { !it.isNullOrEmpty() },
            security = null
        )
    }
}