/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.type

/**
 * Defines how multiple schemas are logically combined in an OpenAPI specification.
 */
public enum class Composition(internal val value: String) {
    /**
     * The data must comply against at least one of the provided schemas.
     *
     * Useful for supporting variations in data structure or type,
     * allowing a field to accept multiple distinct formats or configurations.
     */
    ANY_OF(value = "anyOf"),

    /**
     * The data must validate against all provided schemas simultaneously,
     * allowing for the combination of multiple schema definitions.
     *
     * Often used to add extra requirements or constraints on top of a base schema.
     */
    ALL_OF(value = "allOf"),

    /**
     * The data must validate against exactly one of the provided schemas,
     * ensuring mutual exclusivity among the options.
     *
     * Useful for defining non-overlapping variants of a data structure.
     */
    ONE_OF(value = "oneOf");

    /** Returns the string value representing the OpenAPI format. */
    public operator fun invoke(): String = value

    /** Returns the string value representing the OpenAPI format. */
    override fun toString(): String = value
}
