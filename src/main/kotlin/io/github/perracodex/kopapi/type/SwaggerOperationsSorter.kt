/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.type

/**
 * Represents the sorting options for Swagger UI operations.
 */
public enum class SwaggerOperationsSorter(internal val order: String) {

    /** Keeps operations in the order they are defined in the OpenAPI specification. */
    UNSORTED(order = "unsorted"),

    /** Sorts operations alphabetically by path. */
    ALPHA(order = "alpha"),

    /** Sorts operations by HTTP method (e.g., GET, POST). */
    METHOD(order = "method"),
}
