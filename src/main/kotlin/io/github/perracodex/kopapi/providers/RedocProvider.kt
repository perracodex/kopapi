/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.providers

import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs

/**
 * Provides Redoc resources.
 */
internal object RedocProvider {
    /** The HTML needed to serve the Redoc UI. */
    private lateinit var redocHtml: String

    /**
     * Returns the HTML needed to serve the Redoc UI.
     *
     * @param apiDocs The API documentation configuration.
     * @return The HTML code to serve the Redoc UI.
     */
    fun getRedocHtml(apiDocs: ApiDocs): String {
        if (!this::redocHtml.isInitialized) {
            redocHtml = """
                <!DOCTYPE html>
                <html lang="en">
                    <head>
                        <title>ReDoc API Schema</title>
                        <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
                    </head>
                    <body>
                        <redoc spec-url='${apiDocs.openApiUrl}'></redoc>
                        <script>
                            Redoc.init('${apiDocs.openApiUrl}', {
                                expandSingleSchemaField: true
                            });
                        </script>
                    </body>
                </html>
            """.trimIndent()
        }

        return redocHtml
    }
}
