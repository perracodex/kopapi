/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.security.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builder.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.element.ApiSecurityScheme
import io.github.perracodex.kopapi.type.SecurityLocation
import io.github.perracodex.kopapi.util.sanitize
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Builds an API key security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiOperationBuilder.headerApiKeySecurity]
 * @see [ApiOperationBuilder.queryApiKeySecurity]
 * @see [ApiOperationBuilder.cookieApiKeySecurity]
 * @see [HttpSecurityBuilder]
 * @see [OAuth2SecurityBuilder]
 * @see [OpenIdConnectSecurityBuilder]
 * @see [MutualTLSSecurityBuilder]
 */
@KopapiDsl
public class ApiKeySecurityBuilder internal constructor() {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurityScheme] instance from the current builder state.
     *
     * @param name The name of the security scheme.
     * @param key The name of the header, query parameter, or cookie parameter where the API key is passed.
     * @param location The [SecurityLocation] where the API key is passed.
     * @return The constructed [ApiSecurityScheme] instance.
     */
    @PublishedApi
    internal fun build(name: String, key: String, location: SecurityLocation): ApiSecurityScheme {
        return ApiSecurityScheme.ApiKey(
            schemeName = name.sanitize(),
            key = key.trim(),
            description = description.trimOrNull(),
            location = location
        )
    }
}
