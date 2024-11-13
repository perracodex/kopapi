/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.element

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Builder for the `Info` section of the OpenAPI schema.
 *
 * @property title The title of the API.
 * @property description The description of the API.
 * @property version The version of the API.
 * @property termsOfService The terms of service for the API.
 * @property contact The contact information for the API.
 * @property license The license information for the API.
 */
internal data class ApiInfo @JsonCreator constructor(
    @JsonProperty("title") val title: String = "",
    @JsonProperty("description") val description: String = "",
    @JsonProperty("version") val version: String = "",
    @JsonProperty("termsOfService") val termsOfService: String? = null,
    @JsonProperty("contact") val contact: Contact? = null,
    @JsonProperty("license") val license: License? = null
) {
    /**
     * The `Contact` information in the OpenAPI schema.
     *
     * @property name The name of the contact person/organization.
     * @property url The URL of the contact person/organization.
     * @property email The email of the contact person/organization.
     */
    internal data class Contact(
        val name: String? = null,
        val url: String? = null,
        val email: String? = null
    )

    /**
     * Holds the license information for the API.
     *
     * @param name The name of the license.
     * @param url The URL of the license.
     *
     */
    internal data class License(
        val name: String? = null,
        val url: String? = null
    )
}
