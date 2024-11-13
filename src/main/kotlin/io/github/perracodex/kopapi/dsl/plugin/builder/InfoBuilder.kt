/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.element.ApiInfo
import io.github.perracodex.kopapi.util.string.MultilineString

/**
 * Constructs the information for the API documentation.
 *
 * #### Usage
 * ```
 * info {
 *      title = "API Title"
 *      description = "API Description"
 *      version = "1.0.0"
 *      termsOfService = "https://example.com/terms"
 *
 *      contact {
 *          name = "John Doe"
 *          url = "https://example.com/support"
 *          email = "support@gmail.com"
 *      }
 *
 *      license {
 *          name = "MIT"
 *          url = "https://opensource.org/licenses/MIT"
 *      }
 * }
 *```
 *
 * @property title The title of the API.
 * @property description The description of the API.
 * @property version The version of the API.
 * @property termsOfService The terms of service for the API.
 * @property contact The contact information for the API.
 * @property license The license information for the API.
 */
@KopapiDsl
public class InfoBuilder internal constructor() {
    public var title: String = "API Title"
    public var description: String by MultilineString()
    public var version: String = "1.0.0"
    public var termsOfService: String? = null

    /** Holds a constructed contact object. */
    private var contact: ApiInfo.Contact? = null

    /** Holds a constructed license object. */
    private var license: ApiInfo.License? = null

    /**
     * Builds the contact information for the API.
     *
     * #### Usage
     * ```
     *  contact {
     *      name = "John Doe"
     *      url = "https://example.com/support"
     *      email = "support@example.com"
     *  }
     * ```
     *
     * @receiver [ContactBuilder] The builder used to configure the contact information.
     */
    public fun contact(builder: ContactBuilder.() -> Unit) {
        this.contact = ContactBuilder().apply(builder).build()
    }

    /**
     * Builds the license information for the API.
     *
     * #### Usage
     * ```
     *  license {
     *      name = "MIT"
     *      url = "https://opensource.org/licenses/MIT"
     *  }
     * ```
     *
     * @receiver [LicenseBuilder] The builder used to configure the license information.
     */
    public fun license(builder: LicenseBuilder.() -> Unit) {
        this.license = LicenseBuilder().apply(builder).build()
    }

    /**
     * Produces an immutable [ApiInfo] instance from the builder.
     */
    internal fun build(): ApiInfo = ApiInfo(
        title = title,
        description = description,
        version = version,
        termsOfService = termsOfService,
        contact = contact,
        license = license
    )
}
