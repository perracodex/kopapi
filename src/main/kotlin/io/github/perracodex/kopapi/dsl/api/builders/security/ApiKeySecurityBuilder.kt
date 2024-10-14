/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.api.builders.security

import io.github.perracodex.kopapi.dsl.api.builders.ApiMetadataBuilder
import io.github.perracodex.kopapi.dsl.api.elements.ApiSecurity
import io.github.perracodex.kopapi.dsl.api.types.SecurityLocation
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builds an API key security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiMetadataBuilder.apiKeySecurity]
 * @see [HttpSecurityBuilder]
 * @see [OAuth2SecurityBuilder]
 * @see [OpenIdConnectSecurityBuilder]
 * @see [MutualTLSSecurityBuilder]
 */
public class ApiKeySecurityBuilder {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurity] instance from the current builder state.
     *
     * @param name The name of the security scheme
     * @param location The [SecurityLocation] where the API key is passed.
     * @return The constructed [ApiSecurity] instance.
     */
    @PublishedApi
    internal fun build(name: String, location: SecurityLocation): ApiSecurity {
        return ApiSecurity(
            name = name.trim(),
            description = description.trimOrNull(),
            scheme = ApiSecurity.Scheme.API_KEY,
            location = location
        )
    }
}
