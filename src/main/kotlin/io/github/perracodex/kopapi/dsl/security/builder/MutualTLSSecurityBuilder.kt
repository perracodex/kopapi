/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.security.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builder.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.element.ApiSecurityScheme
import io.github.perracodex.kopapi.util.sanitize
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Builds a Mutual TLS security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiOperationBuilder.mutualTLSSecurity]
 * @see [ApiKeySecurityBuilder]
 * @see [HttpSecurityBuilder]
 * @see [OAuth2SecurityBuilder]
 * @see [OpenIdConnectSecurityBuilder]
 */
@KopapiDsl
public class MutualTLSSecurityBuilder internal constructor() {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurityScheme] instance from the current builder state.
     *
     * @param name The name of the security scheme
     * @return The constructed [ApiSecurityScheme] instance.
     */
    @PublishedApi
    internal fun build(name: String): ApiSecurityScheme {
        return ApiSecurityScheme.MutualTLS(
            schemeName = name.sanitize(),
            description = description.trimOrNull()
        )
    }
}
