/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.markers

import io.ktor.utils.io.*

/**
 * Marks the scope of operation-related DSL builders to avoid accidental
 * invocations between different DSL builders scopes.
 */
@KtorDsl
internal annotation class OperationDsl
