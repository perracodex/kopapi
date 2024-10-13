/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.dsl.builders

import io.github.perracodex.kopapi.plugin.dsl.elements.ApiContact

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
public class ContactBuilder {
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
