/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.ConfigurationDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiLicense
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builder for the `License` information.
 *
 * #### Sample usage
 * ```
 *  license {
 *      name = "MIT"
 *      url = "https://opensource.org/licenses/MIT"
 *  }
 * ```
 *
 * @property name The name of the license.
 * @property url The URL of the license.
 */
@ConfigurationDsl
public class LicenseBuilder {
    public var name: String? = null
    public var url: String? = null

    /**
     * Produces an immutable [ApiLicense] instance from the builder.
     */
    internal fun build(): ApiLicense = ApiLicense(
        name = name.trimOrNull(),
        url = url.trimOrNull()
    )
}
