/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.security

import io.github.perracodex.kopapi.dsl.ApiMetadata
import io.github.perracodex.kopapi.dsl.ApiSecurity
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*

/**
 * A builder for constructing an OpenID Connect security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiMetadata.openIdConnectSecurity]
 */
public class OpenIdConnectSecurityBuilder {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurity] instance from the current builder state.
     *
     * @param name The name of the security scheme
     * @param url The [Url] for the OpenID Connect configuration.
     * @return The constructed [ApiSecurity] instance.
     */
    @PublishedApi
    internal fun build(name: String, url: Url): ApiSecurity {
        return ApiSecurity(
            name = name.trim(),
            description = description.trimOrNull(),
            scheme = ApiSecurity.Scheme.OPENID_CONNECT,
            openIdConnectUrl = url
        )
    }
}