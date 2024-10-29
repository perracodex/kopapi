/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.view.annotation

/**
 * Annotation for controlled access to the DebugView API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the DebugView API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class DebugViewAPI