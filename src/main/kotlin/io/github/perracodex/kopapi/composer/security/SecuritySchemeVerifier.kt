/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.security

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.OAuthFlowType
import io.github.perracodex.kopapi.utils.safeName

@ComposerApi
internal object SecuritySchemeVerifier {
    /**
     * Helper method for consistency checks between global and operation-level security schemes.
     *
     * @throws KopapiException if detected that any security scheme names are not unique
     * between global and Route definitions.
     */
    fun assert(
        global: Set<ApiSecurityScheme>?,
        apiOperations: Set<ApiOperation>
    ) {
        if (!global.isNullOrEmpty() && apiOperations.isNotEmpty()) {
            val globalSchemeMap: Map<String, ApiSecurityScheme> = global.associateBy { it.schemeName.lowercase() }
            val trackedOperationSchemes: MutableMap<String, Pair<ApiSecurityScheme, ApiOperation>> = mutableMapOf()

            apiOperations.forEach { apiOperation ->
                apiOperation.securitySchemes?.forEach { apiOperationScheme ->
                    val globalScheme: ApiSecurityScheme? = globalSchemeMap[apiOperationScheme.schemeName.lowercase()]

                    // Validate OAuth2 schemes.
                    if (globalScheme is ApiSecurityScheme.OAuth2 && apiOperationScheme is ApiSecurityScheme.OAuth2) {
                        validateFlowCompatibility(
                            globalFlows = globalScheme.flows,
                            apiOperationFlows = apiOperationScheme.flows,
                            apiOperation = apiOperation
                        )
                    } else {
                        // Validate non-OAuth2 schemes for full redefinition.
                        validateNonOAuthScheme(
                            globalScheme = globalScheme,
                            apiOperationScheme = apiOperationScheme,
                            apiOperation = apiOperation,
                            trackedOperationSchemes = trackedOperationSchemes
                        )
                    }
                }
            }
        }
    }

    /**
     * Verifies that API operation-level flows don't introduce conflicts with
     * any defined global flows and checks scope consistency.
     *
     * @param globalFlows The top-level global OAuth2 flows.
     * @param apiOperationFlows The API operation-level OAuth2 flows.
     * @param apiOperation The operation that is being validated.
     */
    private fun validateFlowCompatibility(
        globalFlows: ApiSecurityScheme.OAuth2.OAuthFlows,
        apiOperationFlows: ApiSecurityScheme.OAuth2.OAuthFlows,
        apiOperation: ApiOperation
    ) {
        OAuthFlowType.entries.forEach { flowType ->
            val globalFlow: ApiSecurityScheme.OAuth2.OAuthFlow? = flowType.getFlow(flows = globalFlows)
            val apiOperationFlow: ApiSecurityScheme.OAuth2.OAuthFlow? = flowType.getFlow(flows = apiOperationFlows)
            validateFlow(
                globalFlow = globalFlow,
                apiOperationFlow = apiOperationFlow,
                flowType = flowType,
                apiOperation = apiOperation
            )
        }
    }

    /**
     * Validates an individual flow between global and path-level schemes.
     *
     * Ensures that the OAuth2 flow at the operation level is consistent with the global scheme.
     * Only scopes can differ, while properties like `authorizationUrl`, `tokenUrl`, and `refreshUrl`
     * must be identical between global and path-level flows. If these URLs differ, an exception is thrown.
     *
     * @param globalFlow The global OAuth2 flow.
     * @param apiOperationFlow The operation-level OAuth2 flow.
     * @param flowType The type of OAuth2 flow (e.g., authorizationCode, clientCredentials, etc.).
     * @param apiOperation The operation that is being validated.
     */
    private fun validateFlow(
        globalFlow: ApiSecurityScheme.OAuth2.OAuthFlow?,
        apiOperationFlow: ApiSecurityScheme.OAuth2.OAuthFlow?,
        flowType: OAuthFlowType,
        apiOperation: ApiOperation
    ) {
        if (apiOperationFlow != null && globalFlow == null) {
            throw KopapiException(
                "API Operation-level OAuth2 flow '$flowType' is not allowed in API operation:\n" +
                        "   - [${apiOperation.method.value}] → '${apiOperation.path}'\n" +
                        "Reason: It's not part of the top-level global definition.\n"
            )
        }

        // Check if the operation-level flow is defined and validate the properties.
        apiOperationFlow?.let {
            globalFlow?.let {
                validateFlowUrl(
                    globalUrl = globalFlow.authorizationUrl,
                    operationUrl = apiOperationFlow.authorizationUrl,
                    urlType = "authorizationUrl",
                    flowType = flowType,
                    apiOperation = apiOperation
                )
                validateFlowUrl(
                    globalUrl = globalFlow.tokenUrl,
                    operationUrl = apiOperationFlow.tokenUrl,
                    urlType = "tokenUrl",
                    flowType = flowType,
                    apiOperation = apiOperation
                )
                validateFlowUrl(
                    globalUrl = globalFlow.refreshUrl,
                    operationUrl = apiOperationFlow.refreshUrl,
                    urlType = "refreshUrl",
                    flowType = flowType,
                    apiOperation = apiOperation
                )
            }
        }
    }

    /**
     * Validates the flow URLs (authorizationUrl, tokenUrl, refreshUrl) between global and operation-level schemes.
     *
     * Only throws an exception if the operation-level URL is defined and differs from the global one,
     * when such is defined (not null), or if the global URL is undefined but the operation-level URL is provided.
     *
     * @param globalUrl The URL defined in the global OAuth2 flow.
     * @param operationUrl The URL defined in the operation-level OAuth2 flow.
     * @param urlType The type of URL being validated (e.g., "authorizationUrl", "tokenUrl", "refreshUrl").
     * @param flowType The type of OAuth2 flow.
     * @param apiOperation The operation that is being validated.
     */
    private fun validateFlowUrl(
        globalUrl: String?,
        operationUrl: String?,
        urlType: String,
        flowType: OAuthFlowType,
        apiOperation: ApiOperation
    ) {
        // If operation-level URL is defined but global URL is not, throw an exception.
        if (operationUrl != null && globalUrl == null) {
            throw KopapiException(
                "The operation-level '$urlType' is defined for flow '$flowType' in:\n" +
                        "   - [${apiOperation.method.value}] → '${apiOperation.path}'\n" +
                        "but there is no corresponding top-level global definition for this URL.\n" +
                        "To resolve:\n" +
                        "   1. Either remove the API Operation-level '$urlType' to inherit the top-level global definition.\n" +
                        "   2. Or, define this URL also globally for the '$flowType' flow.\n"
            )
        }

        // If both URLs are defined, and they differ, throw an exception.
        if (globalUrl != null && operationUrl != null && globalUrl != operationUrl) {
            throw KopapiException(
                "Mismatch in '$urlType' for flow '$flowType' in:\n" +
                        "   - API Operation: [${apiOperation.method.value}] → '${apiOperation.path}':\n" +
                        "   - Global level: ${globalUrl}, API Operation-level: ${operationUrl}.\n" +
                        "To resolve:\n" +
                        "   1. Ensure that the API Operation-level '$urlType' matches the global value.\n" +
                        "   2. Or, remove the API Operation-level definition to inherit the global value.\n"
            )
        }
    }

    /**
     * Validates non-OAuth2 security schemes (e.g., HTTP, API Key, OpenID Connect, Mutual TLS)
     * between global and operation-level schemes.
     * Non-OAuth2 schemes cannot be partially overridden, so they need to be fully redefined.
     *
     * @param globalScheme The global security scheme.
     * @param apiOperationScheme The operation-level security scheme.
     * @param apiOperation The operation being validated.
     * @param trackedOperationSchemes Tracks redefined schemes across operations to detect inconsistencies.
     */
    private fun validateNonOAuthScheme(
        globalScheme: ApiSecurityScheme?,
        apiOperationScheme: ApiSecurityScheme?,
        apiOperation: ApiOperation,
        trackedOperationSchemes: MutableMap<String, Pair<ApiSecurityScheme, ApiOperation>>
    ) {
        val schemeName: String = apiOperationScheme?.schemeName ?: return

        // Check if the operation scheme redefines a global scheme.
        if (globalScheme != null && apiOperationScheme != globalScheme) {
            throw KopapiException(
                "Scheme '$schemeName' is defined in both top-level global configuration and API Operation:\n" +
                        "   - [${apiOperation.method.value}] → '${apiOperation.path}'\n" +
                        "Non-OAuth2 schemes (like '${apiOperationScheme::class.safeName()}') cannot be overridden.\n" +
                        "To resolve:\n" +
                        "   1. Ensure the scheme '$schemeName' is fully redefined only at the API Operation level.\n" +
                        "   2. Or, change the name to avoid conflicts with the top-level global scheme.\n"
            )
        }

        // Track and compare the scheme within different operations to detect redefinitions.
        trackedOperationSchemes[schemeName.lowercase()]?.let { trackedScheme ->
            throw KopapiException(
                "Scheme '$schemeName' is defined in multiple API Operations but with different properties:\n" +
                        "   - [${apiOperation.method.value}] → '${apiOperation.path}'\n" +
                        "   - [${trackedScheme.second.method.value}] → '${trackedScheme.second.path}'\n" +
                        "To resolve:\n" +
                        "   1. Ensure that the security scheme '$schemeName' has the same properties across all API Operations.\n" +
                        "   2. Or, give the API Operation-level scheme a unique name to differentiate it from other definitions.\n"
            )
        } ?: run {
            // First occurrence, track the scheme.
            trackedOperationSchemes[schemeName.lowercase()] = apiOperationScheme to apiOperation
        }
    }
}
