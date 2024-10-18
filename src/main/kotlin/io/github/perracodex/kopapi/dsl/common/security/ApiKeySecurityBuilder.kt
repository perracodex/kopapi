/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.security

import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.types.SecurityLocation
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builds an API key security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiOperationBuilder.apiKeySecurity]
 * @see [HttpSecurityBuilder]
 * @see [OAuth2SecurityBuilder]
 * @see [OpenIdConnectSecurityBuilder]
 * @see [MutualTLSSecurityBuilder]
 */
public class ApiKeySecurityBuilder {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurityScheme] instance from the current builder state.
     *
     * @param name The name of the security scheme.
     * @param apiKeyName The name of the header, query parameter, or cookie parameter where the API key is passed.
     * @param location The [SecurityLocation] where the API key is passed.
     * @return The constructed [ApiSecurityScheme] instance.
     */
    @PublishedApi
    internal fun build(name: String, apiKeyName: String, location: SecurityLocation): ApiSecurityScheme {
        return ApiSecurityScheme.ApiKey(
            schemeName = name.trim(),
            apiKeyName = apiKeyName.trim(),
            description = description.trimOrNull(),
            location = location
        )
    }
}
