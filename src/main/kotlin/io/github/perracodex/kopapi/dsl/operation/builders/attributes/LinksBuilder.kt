/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.attributes

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.sanitize

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
     * #### Sample Usage
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
     * @param name The unique name of the link.
     * @param configure A lambda receiver for configuring the [LinkBuilder].
     * @throws KopapiException If a link with the same operation ID already exists.
     */
    public fun add(name: String, configure: LinkBuilder.() -> Unit) {
        val linkName: String = name.sanitize()
        if (linkName.isBlank()) {
            throw KopapiException("Link name must not be blank.")
        }
        val link: ApiLink = LinkBuilder().apply(configure).build()
        links[linkName] = link
    }

    /**
     * Builds the links collection.
     *
     * @return A set of [ApiLink] instances.
     */
    internal fun build(): MutableMap<String, ApiLink> = links
}
