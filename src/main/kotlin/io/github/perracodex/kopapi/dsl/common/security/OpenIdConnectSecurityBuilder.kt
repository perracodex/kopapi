/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.security

import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*

/**
 * Builds an OpenID Connect security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiOperationBuilder.openIdConnectSecurity]
 * @see [ApiKeySecurityBuilder]
 * @see [HttpSecurityBuilder]
 * @see [OAuth2SecurityBuilder]
 * @see [MutualTLSSecurityBuilder]
 */
public class OpenIdConnectSecurityBuilder {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurityScheme] instance from the current builder state.
     *
     * @param name The name of the security scheme
     * @param url The [Url] for the OpenID Connect configuration.
     * @return The constructed [ApiSecurityScheme] instance.
     */
    @PublishedApi
    internal fun build(name: String, url: Url): ApiSecurityScheme {
        return ApiSecurityScheme.OpenIdConnect(
            schemeName = name.trim(),
            description = description.trimOrNull(),
            openIdConnectUrl = url.toString()
        )
    }
}
