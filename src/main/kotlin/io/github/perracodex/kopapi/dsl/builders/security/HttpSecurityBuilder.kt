/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.security

import io.github.perracodex.kopapi.dsl.builders.ApiMetadataBuilder
import io.github.perracodex.kopapi.dsl.elements.ApiSecurity
import io.github.perracodex.kopapi.dsl.types.AuthenticationMethod
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builds an HTTP security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiMetadataBuilder.httpSecurity]
 * @see [ApiKeySecurityBuilder]
 * @see [OAuth2SecurityBuilder]
 * @see [OpenIdConnectSecurityBuilder]
 * @see [MutualTLSSecurityBuilder]
 */
public class HttpSecurityBuilder {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurity] instance from the current builder state.
     *
     * @param name The name of the security scheme
     * @param method The [AuthenticationMethod] of the security scheme.
     * @return The constructed [ApiSecurity] instance.
     */
    @PublishedApi
    internal fun build(name: String, method: AuthenticationMethod): ApiSecurity {
        return ApiSecurity(
            name = name.trim(),
            description = description.trimOrNull(),
            scheme = ApiSecurity.Scheme.HTTP,
            authenticationMethod = method
        )
    }
}
