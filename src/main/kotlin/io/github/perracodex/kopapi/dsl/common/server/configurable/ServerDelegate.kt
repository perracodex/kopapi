/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.server.configurable

import io.github.perracodex.kopapi.dsl.common.server.ServerBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig

/**
 * Configurable handling server registration.
 */
@KopapiDsl
internal class ServerDelegate : IServerConfigurable {
    internal val servers: MutableSet<ApiServerConfig> = mutableSetOf()

    override fun servers(builder: ServerBuilder.() -> Unit) {
        val serverBuilder: ServerBuilder = ServerBuilder().apply(builder)
        servers.addAll(serverBuilder.build())
    }
}
