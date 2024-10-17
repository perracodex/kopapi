/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.types

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Enum defining possible serialization styles for parameters.
 *
 * @property value The string value of the parameter style.
 */
public enum class ParameterStyle(@JsonValue internal val value: String) {
    /** Commonly used for query parameters, formatted as key-value pairs. */
    FORM(value = "form"),

    /** Commonly used for headers, presented in a simple, compact format. */
    SIMPLE(value = "simple"),

    /** Serializes arrays by separating values with spaces. e.g., "a b c" */
    SPACE_DELIMITED(value = "spaceDelimited"),

    /** Serializes arrays by separating values with pipes. e.g., "a|b|c" */
    PIPE_DELIMITED(value = "pipeDelimited"),

    /** Prefixes parameters with a period and uses dot-separated values. e.g., ".a.b.c" */
    LABEL(value = "label"),

    /** Uses semicolons to separate object properties in paths. e.g., ";a=1;b=2" */
    MATRIX(value = "matrix"),
}
