/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.dsl.ApiMetadata
import io.github.perracodex.kopapi.routing.collectRouteAttributes
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*

/**
 * Builder for the API metadata and schemas.
 */
internal object SchemaProvider {
    /**
     * Defines the key used to store and retrieve API metadata associated with a specific [Route].
     *
     * This key is used as part of the routing configuration process, where API metadata is attached to routes
     * using the `api` extension function.
     */
    val ApiMetadataKey: AttributeKey<ApiMetadata> = AttributeKey(name = "ApiMetadata")

    /** The full API metadata. */
    private var apiMetadata: List<ApiMetadata>? = null

    /** The full API metadata in JSON format. */
    private var apiMetadataJson: String? = null

    /**
     * Get the full API metadata.
     *
     * @param application The [Application] reference with the routes to traverse.
     * @return The list of [ApiMetadata] objects.
     */
    fun getApiMetadata(application: Application): List<ApiMetadata> {
        return apiMetadata ?: run {
            val collectedApiMetadata: List<ApiMetadata> = application.collectRouteAttributes(attributeKey = ApiMetadataKey)
            apiMetadata = collectedApiMetadata
            collectedApiMetadata
        }
    }

    /**
     * Get the full API metadata in JSON format.
     *
     * @param application The [Application] reference with the routes to traverse.
     * @return The JSON string of the list of [ApiMetadata] objects.
     */
    fun getApiMetadataJson(application: Application): String {
        return apiMetadataJson ?: run {
            val apiMetadata: List<ApiMetadata> = getApiMetadata(application = application)
            val apiMetadataJsonResult: String = SerializationUtils.toJson(instance = apiMetadata)
            apiMetadataJson = apiMetadataJsonResult
            apiMetadataJsonResult
        }
    }
}
