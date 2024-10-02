/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*

/**
 * Extension function traverse all routes and collect a given attribute from each route.
 *
 * @param attributeKey The attribute key to collect from each route.
 * @return A list of all found attribute values.
 */
internal fun <T : Any> Application.collectRouteAttributes(attributeKey: AttributeKey<T>): List<T> {
    val attributeValues: MutableList<T> = mutableListOf()

    // Helper function to recursively traverse routes and collect attribute values.
    fun Route.collectAttributes() {
        this.attributes.getOrNull(key = attributeKey)?.let {
            attributeValues.add(it)
        }
        // Recursively collect attributes from child routes.
        this.children.forEach { it.collectAttributes() }
    }

    // Start collecting from the root route.
    this.routing { }.collectAttributes()

    return attributeValues
}

