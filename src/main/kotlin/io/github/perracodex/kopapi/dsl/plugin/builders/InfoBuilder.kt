/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.ConfigurationDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiContact
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiLicense
import io.github.perracodex.kopapi.utils.string.MultilineString

/**
 * Constructs the information for the API documentation.
 *
 * #### Sample usage
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
 * Note that the description can be declared multiple times to support multi-line descriptions,
 * without the need for manual line breaks. Each of the descriptions will be concatenated into
 * a single description divided by a new line.
 *
 * ```
 * {
 *     ...
 *     description = "Api Description."
 *     description = "This is a multi-line description."
 *     description = "Each description will be concatenated into one."
 *     ...
 * }
 * ```
 *
 * Will result in the following description:
 *
 * ```text
 * Api Description.
 * This is a multi-line description.
 * Each description will be concatenated into a single description.
 * ```
 *
 * @property title The title of the API.
 * @property description The description of the API.
 * @property version The version of the API.
 * @property termsOfService The terms of service for the API.
 * @property contact The contact information for the API.
 * @property license The license information for the API.
 */
@ConfigurationDsl
public class InfoBuilder {
    public var title: String = "API Title"
    public var description: String by MultilineString()
    public var version: String = "1.0.0"
    public var termsOfService: String? = null
    private var contact: ApiContact? = null
    private var license: ApiLicense? = null

    /**
     * Builds the contact information for the API.
     *
     * #### Sample usage
     * ```
     *  contact {
     *      name = "John Doe"
     *      url = "https://example.com/support"
     *      email = "support@example.com"
     *  }
     * ```
     *
     * @see [ContactBuilder]
     */
    public fun contact(init: ContactBuilder.() -> Unit) {
        this.contact = ContactBuilder().apply(init).build()
    }

    /**
     * Builds the license information for the API.
     *
     * #### Sample usage
     * ```
     *  license {
     *      name = "MIT"
     *      url = "https://opensource.org/licenses/MIT"
     *  }
     * ```
     *
     * @see [LicenseBuilder]
     */
    public fun license(init: LicenseBuilder.() -> Unit) {
        this.license = LicenseBuilder().apply(init).build()
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
