/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.markers

/**
 * Marks the scope of configuration-related DSL builders to avoid accidental
 * invocations between different DSL builders scopes.
 */
@DslMarker
internal annotation class ConfigurationDsl
