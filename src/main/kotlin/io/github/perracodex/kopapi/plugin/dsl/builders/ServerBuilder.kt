/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.dsl.builders

import io.ktor.http.*

/**
 * Represents a collection of servers with enforced uniqueness.
 */
public class ServersBuilder {
    /** The internal set of servers. */
    private val internalSet: MutableSet<Url> = mutableSetOf()

    /**
     * Adds a new server [Url].
     *
     * @param url The URL to add.
     */
    public fun add(url: Url) {
        if (url.toString().isNotBlank()) {
            internalSet.add(url)
        }
    }

    /**
     * Adds a new server [Url].
     *
     * @param urlString Add [Url] from `urlString`.
     */
    public fun add(urlString: String) {
        if (urlString.isNotBlank()) {
            internalSet.add(Url(urlString = urlString))
        }
    }

    /**
     * Returns a read-only view of the servers.
     * If no servers are registered, a default server is returned.
     *
     * @return A read-only set of the servers.
     */
    internal fun get(): Set<Url> {
        if (this.internalSet.isEmpty()) {
            return setOf(Url(DEFAULT_SERVER))
        }
        return internalSet.toSet()
    }

    /**
     * Checks if the servers collection is empty.
     */
    internal fun isEmpty(): Boolean {
        return internalSet.isEmpty()
    }

    override fun toString(): String = internalSet.toString()

    private companion object {
        /** The default server URL. Used when no servers are provided. */
        const val DEFAULT_SERVER = "http://localhost:8080"
    }
}
