/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.security

import io.github.perracodex.kopapi.dsl.markers.SecurityDsl
import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.types.AuthenticationMethod
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.utils.io.*

/**
 * Builds an HTTP security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiOperationBuilder.httpSecurity]
 * @see [ApiKeySecurityBuilder]
 * @see [OAuth2SecurityBuilder]
 * @see [OpenIdConnectSecurityBuilder]
 * @see [MutualTLSSecurityBuilder]
 */
@KtorDsl
@SecurityDsl
public class HttpSecurityBuilder {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurityScheme] instance from the current builder state.
     *
     * @param name The name of the security scheme
     * @param method The [AuthenticationMethod] of the security scheme.
     * @return The constructed [ApiSecurityScheme] instance.
     */
    @PublishedApi
    internal fun build(name: String, method: AuthenticationMethod): ApiSecurityScheme {
        return ApiSecurityScheme.Http(
            schemeName = name.trim(),
            description = description.trimOrNull(),
            method = method
        )
    }
}
