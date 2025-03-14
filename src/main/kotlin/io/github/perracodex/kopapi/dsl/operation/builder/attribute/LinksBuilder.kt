/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.operation.builder.attribute

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.element.ApiLink
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.sanitize

/**
 * Builds a collection of response links for an API endpoint.
 */
@KopapiDsl
public class LinksBuilder @PublishedApi internal constructor() {
    /** Cached links. */
    private val links: MutableMap<String, ApiLink> = mutableMapOf()

    /**
     * Adds a link to the collection.
     *
     * #### Usage
     * ```
     * links {
     *      add(name = "GetEmployeeDetails") {
     *          operationId = "getEmployeeDetails"
     *          description = "Retrieve information about this employee."
     *          parameter(
     *              name = "employee_id",
     *              value = "\$request.path.employee_id"
     *          )
     *      }
     *      add(name = "UpdateEmployeeStatus") {
     *          operationId = "updateEmployeeStatus"
     *          description = "Link to update the status of this employee."
     *          parameter(
     *              name = "employee_id",
     *              value = "\$request.path.employee_id"
     *          )
     *          parameter(name = "status", value = "active")
     *          requestBody = "{\"status\": \"active\"}"
     *      }
     *      add(name = "ListEmployeeBenefits") {
     *          operationRef = "/api/v1/benefits/list"
     *          description = "List all benefits available to the employee."
     *          parameter(
     *              name = "employee_id",
     *              value = "\$request.path.employee_id"
     *          )
     *      }
     * }
     * ```
     *
     * @receiver [LinkBuilder] The builder used to configure the link.
     *
     * @param name The unique name of the link.
     * @throws KopapiException If a link with the same operation ID already exists.
     */
    public fun add(name: String, builder: LinkBuilder.() -> Unit) {
        val linkName: String = name.sanitize()
        if (linkName.isBlank()) {
            throw KopapiException("Link name must not be blank.")
        }
        val link: ApiLink = LinkBuilder().apply(builder).build()
        links[linkName] = link
    }

    /**
     * Builds the links collection.
     *
     * @return A set of [ApiLink] instances.
     */
    internal fun build(): MutableMap<String, ApiLink>? = links.orNull()
}
