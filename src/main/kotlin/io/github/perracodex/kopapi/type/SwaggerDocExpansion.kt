/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.type


/**
 * Represents the different expansion states for Swagger UI documentation.
 */
public enum class SwaggerDocExpansion(internal val value: String) {
    /**
     * Collapse all documentation, showing only the headings.
     *
     * Useful for large APIs where expanded endpoints can be overwhelming.
     */
    NONE(value = "none"),

    /**
     * Show only the list of operations within each resource without expanding details.
     *
     * Provides a middle ground, showing an overview but hiding the full details.
     */
    LIST(value = "list"),

    /**
     * Expand all details for each endpoint by default.
     *
     * Suitable for smaller APIs where users want to view all details at once.
     */
    FULL(value = "full");
}
