/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.types

/**
 * Available themes for Swagger UI.
 *
 * @property themeName The name of the theme used in Swagger UI configuration.
 */
public enum class SwaggerUiTheme(internal val themeName: String) {
    /** Light theme, used as the default theme in Swagger UI. */
    LIGHT(themeName = "light"),

    /** Dark theme, providing a more comfortable experience in low-light environments. */
    DARK(themeName = "dark"),
}
