/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.annotation

/**
 * Annotation for controlled access to the TypeInspector API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the TypeInspector API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class TypeInspectorAPI
