/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.inspector.schema.Schema

/**
 * Represents the [Schema] definition for content in an API operation.
 *
 * This class is used to wrap the schema under the `schema` key for any content type
 * (e.g., `application/json`, `application/xml`), ensuring compatibility with the OpenAPI
 * specification.
 *
 * @property schema The [Schema] representing the structure of the content.
 */
internal data class ContentSchema(
    @JsonProperty("schema")
    val schema: Schema
)
