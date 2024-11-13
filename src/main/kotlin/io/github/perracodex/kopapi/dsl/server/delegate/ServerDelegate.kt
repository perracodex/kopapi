/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.server.delegate

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig
import io.github.perracodex.kopapi.dsl.server.builder.ServerBuilder
import io.github.perracodex.kopapi.util.orNull

/**
 * Configurable handling server registration.
 */
@KopapiDsl
internal class ServerDelegate : IServerConfigurable {
    /** Cached servers. */
    private val servers: MutableSet<ApiServerConfig> = mutableSetOf()

    override fun servers(builder: ServerBuilder.() -> Unit) {
        val serverBuilder: ServerBuilder = ServerBuilder().apply(builder)
        serverBuilder.build()?.let { servers.addAll(it) }
    }

    /**
     * Returns the registered server configurations.
     */
    fun build(): Set<ApiServerConfig>? = servers.orNull()
}
