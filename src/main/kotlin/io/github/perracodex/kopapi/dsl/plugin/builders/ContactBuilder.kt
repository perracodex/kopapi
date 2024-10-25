/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.ConfigurationDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiContact
import io.ktor.utils.io.*

/**
 * Builder for the `Contact` information.
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
 * @property name The name of the contact person/organization.
 * @property url The URL of the contact person/organization.
 * @property email The email of the contact person/organization.
 */
@KtorDsl
@ConfigurationDsl
public class ContactBuilder internal constructor() {
    public var name: String? = null
    public var url: String? = null
    public var email: String? = null

    /**
     * Produces an immutable [ApiContact] instance from the builder.
     */
    internal fun build(): ApiContact = ApiContact(
        name = name,
        url = url,
        email = email
    )
}
