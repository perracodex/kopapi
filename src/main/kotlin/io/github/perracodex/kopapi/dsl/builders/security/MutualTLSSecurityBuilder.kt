/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.security

import io.github.perracodex.kopapi.dsl.ApiMetadata
import io.github.perracodex.kopapi.dsl.ApiSecurity
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * A builder for constructing Mutual TLS security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [ApiMetadata.mutualTLSSecurity]
 */
public class MutualTLSSecurityBuilder {
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
            scheme = ApiSecurity.Scheme.MUTUAL_TLS
        )
    }
}