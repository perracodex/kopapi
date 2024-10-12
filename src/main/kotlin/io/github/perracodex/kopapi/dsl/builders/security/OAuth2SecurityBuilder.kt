/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.security

import io.github.perracodex.kopapi.dsl.builders.ApiMetadataBuilder
import io.github.perracodex.kopapi.dsl.elements.ApiSecurity
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * A builder for constructing an OAuth2 security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiMetadataBuilder.oauth2Security]
 */
public class OAuth2SecurityBuilder {
    public var description: String by MultilineString()

    /**
     * Builds an [ApiSecurity] instance from the current builder state.
     *
     * @param name The name of the security scheme
     * @return The constructed [ApiSecurity] instance.
     */
    @PublishedApi
    internal fun build(name: String): ApiSecurity {
        return ApiSecurity(
            name = name.trim(),
            description = description.trimOrNull(),
            scheme = ApiSecurity.Scheme.OAUTH2
        )
    }
}
