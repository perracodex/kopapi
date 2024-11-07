/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
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
@KopapiDsl
public class LicenseBuilder internal constructor() {
    public var name: String? = null
    public var url: String? = null

    /**
     * Produces an immutable [ApiInfo.License] instance from the builder.
     */
    internal fun build(): ApiInfo.License = ApiInfo.License(
        name = name.trimOrNull(),
        url = url.trimOrNull()
    )
}
