/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.type

/**
 * Represents the syntax highlighting themes for Swagger UI.
 */
public enum class SwaggerSyntaxTheme(internal val themeName: String) {
    /** Agate theme, often used as the default theme in Swagger UI. */
    AGATE(themeName = "agate"),

    /** Arta theme, providing a colorful syntax highlighting scheme. */
    ARTA(themeName = "arta"),

    /** Idea theme, based on IntelliJ IDEA’s code colors. */
    IDEA(themeName = "idea"),

    /** Monokai theme, known for its vibrant colors and dark background. */
    MONOKAI(themeName = "monokai"),

    /** Nord theme, featuring soft colors inspired by arctic landscapes. */
    NORD(themeName = "nord"),

    /** None theme, disabling syntax highlighting in Swagger UI. */
    NONE(themeName = "none"),

    /** Obsidian theme, with a dark palette suited for low-light environments. */
    OBSIDIAN(themeName = "obsidian"),

    /** Tomorrow Night theme, offering a clean, modern dark scheme. */
    TOMORROW_NIGHT(themeName = "tomorrow-night"),
}
