/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.security

import io.github.perracodex.kopapi.dsl.markers.SecurityDsl
import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.types.AuthMethod
import io.github.perracodex.kopapi.utils.sanitize
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builds an HTTP security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiOperationBuilder.basicSecurity]
 * @see [ApiOperationBuilder.bearerSecurity]
 * @see [ApiOperationBuilder.digestSecurity]
 * @see [ApiKeySecurityBuilder]
 * @see [OAuth2SecurityBuilder]
 * @see [OpenIdConnectSecurityBuilder]
 * @see [MutualTLSSecurityBuilder]
 */
@SecurityDsl
public class HttpSecurityBuilder {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurityScheme] instance from the current builder state.
     *
     * @param name The name of the security scheme
     * @param method The [AuthMethod] of the security scheme.
     * @return The constructed [ApiSecurityScheme] instance.
     */
    @PublishedApi
    internal fun build(name: String, method: AuthMethod): ApiSecurityScheme {
        return ApiSecurityScheme.Http(
            schemeName = name.sanitize(),
            description = description.trimOrNull(),
            method = method
        )
    }
}
