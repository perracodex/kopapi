/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.attributes

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.github.perracodex.kopapi.system.KopapiException

/**
 * Builds a collection of response links for an API endpoint.
 *
 * #### Sample Usage
 * ```
 * links {
 *     link("getNextItem") {
 *         description = "Link to the next item."
 *     }
 *
 *     link("getPreviousItem") {
 *         description = "Link to the previous item."
 *     }
 * }
 * ```
 *
 * @property links A mutable set holding the configured [ApiLink] instances.
 */
@KopapiDsl
public class LinksBuilder @PublishedApi internal constructor() {
    @PublishedApi
    internal val links: MutableSet<ApiLink> = mutableSetOf()

    /**
     * Adds a link to the collection.
     *
     * @param operationId The name of an existing, resolvable OAS operation.
     * @param configure A lambda receiver for configuring the [LinkBuilder].
     *
     * #### Sample Usage
     * ```
     * link("getNextItem") {
     *     description = "Link to the next item."
     * }
     * ```
     *
     * @throws KopapiException If a link with the same operation ID already exists.
     */
    public fun link(operationId: String, configure: LinkBuilder.() -> Unit) {
        val link: ApiLink = LinkBuilder(operationId = operationId).apply(configure).build()
        addLink(link)
    }

    /**
     * Adds a link to the collection, ensuring uniqueness based on [ApiLink.operationId].
     *
     * @param link The [ApiLink] instance to add.
     * @throws KopapiException If a link with the same operation ID already exists.
     */
    internal fun addLink(link: ApiLink) {
        if (links.any { it.operationId.equals(link.operationId, ignoreCase = true) }) {
            throw KopapiException("Link with operation ID '${link.operationId}' already exists within the same response.")
        }
        links.add(link)
    }

    /**
     * Builds the links collection.
     *
     * @return A set of [ApiLink] instances.
     */
    internal fun build(): Set<ApiLink> = links
}
