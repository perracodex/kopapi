/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.security

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration

/**
 * Responsible for composing the security-related sections of the OpenAPI schema.
 *
 * Aggregates global security requirements, security schemes, and associates
 * security configurations with individual API Operations based on their metadata.
 *
 * @property apiConfiguration The plugin [ApiConfiguration] containing global security schemes.
 * @property apiOperations A set of [ApiOperation] objects representing each API endpoint's metadata.
 */
@ComposerAPI
internal class SecuritySectionComposer(
    private val apiConfiguration: ApiConfiguration,
    private val apiOperations: Set<ApiOperation>
) {
    /**
     * Determines and composes the global security requirements for the OpenAPI schema.
     *
     * Global security requirements apply to all API operations by default, unless overridden
     * by operation-level security configurations. This method includes OAuth2 schemes
     * only if they have scopes defined within their flows.
     *
     * @return A [GlobalSecurityRequirement] object containing all global security requirements.
     *         Returns `null` if no operations require global security.
     */
    fun composeGlobalSecurityRequirements(): GlobalSecurityRequirement? {
        // Determine if any API Operation requires security.
        val requiresGlobalSecurity: Boolean = apiOperations.any { !it.skipSecurity }
        if (!requiresGlobalSecurity) {
            // All API Operated are marked as skipSecurity; no global security required.
            return null
        }

        // Aggregate global security schemes.
        val requirements: List<SecurityRequirement> = apiConfiguration
            .apiSecuritySchemes?.map(this::mapSchemeToRequirement) ?: emptyList()

        // Only return if there are security schemes defined
        return if (requirements.isNotEmpty()) {
            GlobalSecurityRequirement(requirements = requirements)
        } else {
            null
        }
    }

    /**
     * Aggregates both top-level global and operation-level security schemes for inclusion
     * in the OpenAPI components section.
     *
     * Collects top-level global security schemes and adds them to the components section.
     * Additionally, operation-level security schemes are included if they are not already part
     * of the global schemes, ensuring that all available security mechanisms are represented
     * in the components section.
     *
     * @return A map where each key is the security scheme name and the value is the corresponding
     *         [ApiSecurityScheme] definition. Returns `null` if no security schemes are defined.
     *
     * @see [composeGlobalSecurityRequirements]
     */
    fun composeSecuritySchemes(): Map<String, ApiSecurityScheme>? {
        val schemes: MutableMap<String, ApiSecurityScheme> = mutableMapOf()
        val schemeNames: MutableSet<String> = mutableSetOf()

        // Include global security schemes only if global security is applied.
        composeGlobalSecurityRequirements()?.let {
            apiConfiguration.apiSecuritySchemes?.forEach { scheme ->
                val schemeName: String = scheme.schemeName.lowercase()
                if (schemeName !in schemeNames) {
                    schemes[scheme.schemeName] = scheme
                    schemeNames.add(schemeName)
                }
            }
        }

        // Include per-operation security schemes for operations that require security.
        // If `skipSecurity` is set, the operation does not require security, so it is
        // assumed that either no security is needed at all, or any defined one
        // in the operation should be ignored.
        apiOperations.forEach { operation ->
            if (!operation.skipSecurity) {
                operation.securitySchemes?.forEach { scheme ->
                    val schemeName: String = scheme.schemeName.lowercase()
                    if (schemeName !in schemeNames) {
                        schemes[scheme.schemeName] = scheme
                        schemeNames.add(schemeName)
                    }
                }
            }
        }

        return schemes.toSortedMap().takeIf { it.isNotEmpty() }
    }

    /**
     * Associates each API operation with its specific security requirements.
     *
     * Creates a list of [OperationSecurity] objects, each linking an API operation
     * (identified by its HTTP method and path) with the relevant security schemes.
     * API Operations that do not require security are associated with an empty security
     * list (`security: []`), effectively disabling security for those endpoints.
     *
     * @return A list of [OperationSecurity] objects representing the security configuration
     *         for each API operation. Returns `null` if no operations are present.
     */
    fun composeOperationSecurity(): List<OperationSecurity>? {
        val operationSecurityList: MutableList<OperationSecurity> = mutableListOf()

        apiOperations.forEach { operation ->
            // If skipSecurity is set, explicitly disable security by assigning an empty list.
            if (operation.skipSecurity) {
                operationSecurityList.add(
                    OperationSecurity(
                        method = operation.method.value,
                        path = operation.path,
                        security = emptyList() // This will produce `security: []` in OpenAPI.
                    )
                )
            } else {
                // Operation requires security; map each security scheme to a SecurityRequirement.
                val securityRequirements: List<SecurityRequirement> =
                    operation.securitySchemes?.map(this::mapSchemeToRequirement) ?: emptyList()

                operationSecurityList.add(
                    OperationSecurity(
                        method = operation.method.value,
                        path = operation.path,
                        security = securityRequirements.takeIf { it.isNotEmpty() }
                    )
                )
            }
        }

        return operationSecurityList.takeIf { it.isNotEmpty() }
    }

    /**
     * Helper function to map a security scheme to a [SecurityRequirement], considering `OAuth2` scopes.
     *
     * @param scheme The security scheme to map.
     * @return A resolved [SecurityRequirement] object, or `null` if not a `OAuth2` scheme.
     */
    private fun mapSchemeToRequirement(scheme: ApiSecurityScheme): SecurityRequirement {
        val scopes: List<String>? = when {
            scheme is ApiSecurityScheme.OAuth2 -> collectAllScopes(flows = scheme.flows).distinct()
            else -> null
        }

        return SecurityRequirement(securityScheme = scheme, scopes = scopes)
    }

    /**
     * Collects all scopes from the provided OAuth2 flows.
     *
     * @param flows The [ApiSecurityScheme.OAuth2.OAuthFlows] containing various OAuth2 flow configurations.
     * @return A list of all unique scopes defined across the flows.
     */
    private fun collectAllScopes(flows: ApiSecurityScheme.OAuth2.OAuthFlows): List<String> {
        val scopes: MutableList<String> = mutableListOf()
        return with(flows) {
            implicit?.scopes?.keys?.let { scopes.addAll(it) }
            password?.scopes?.keys?.let { scopes.addAll(it) }
            clientCredentials?.scopes?.keys?.let { scopes.addAll(it) }
            authorizationCode?.scopes?.keys?.let { scopes.addAll(it) }
        }.let { scopes.distinct() }
    }
}
