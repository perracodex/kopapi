/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.provider

import io.github.perracodex.kopapi.dsl.plugin.element.ApiDocs
import io.github.perracodex.kopapi.type.SwaggerOperationsSorter
import io.github.perracodex.kopapi.type.SwaggerSyntaxTheme
import io.github.perracodex.kopapi.type.SwaggerUiTheme
import io.ktor.http.content.*
import io.ktor.server.application.*

/**
 * Providers Swagger resources.
 */
internal object SwaggerProvider {
    /** Path to the resources within the WebJar. */
    private const val WEBJARS_PATH: String = "META-INF/resources/webjars/swagger-ui/"

    /** Path to the properties file that holds the version information. */
    private const val PROPERTIES_PATH: String = "META-INF/maven/org.webjars/swagger-ui/pom.properties"

    /** Cached path to the resources within the WebJar. */
    private lateinit var swaggerPath: String

    /** The  JavaScript needed to initialize the Swagger UI. */
    private lateinit var swaggerJs: String

    /**
     * Retrieves the content for the specified file from the webjar resources.
     *
     * It attempts to locate and serve a specific file (e.g., JavaScript, HTML)
     * from the Swagger UI resources.
     *
     * @param environment The application environment to access resources.
     * @param filename The name of the file to retrieve (e.g., "index.html").
     * @return The outgoing content representing the specified file, or `null` if the file is not found.
     */
    fun getContentFor(environment: ApplicationEnvironment, filename: String): OutgoingContent? {
        if (!this::swaggerPath.isInitialized) {
            val swaggerUiVersion: String = WebJarProvider.getVersion(
                environment = environment,
                propertiesPath = PROPERTIES_PATH
            )
            swaggerPath = "$WEBJARS_PATH$swaggerUiVersion"
        }

        return WebJarProvider.getContentFor(
            environment = environment,
            path = swaggerPath,
            filename = filename
        )?.let { return it }
    }

    /**
     * Returns the JavaScript code needed to initialize Swagger UI with the provided OpenAPI URL.
     *
     * @param apiDocs The API documentation configuration.
     * @return The JavaScript code to initialize Swagger UI.
     */
    fun getSwaggerInitializer(apiDocs: ApiDocs): String {
        if (!this::swaggerJs.isInitialized) {
            val sorterSetting: String = when (apiDocs.swagger.operationsSorter) {
                SwaggerOperationsSorter.UNSORTED -> ""
                else -> "operationsSorter: \"${apiDocs.swagger.operationsSorter.order}\","
            }

            val syntaxThemeSetting: String = when (apiDocs.swagger.syntaxTheme) {
                SwaggerSyntaxTheme.NONE -> "syntaxHighlight: false,"
                else -> "syntaxHighlight: { theme: \"${apiDocs.swagger.syntaxTheme.themeName}\" },"
            }

            val operationIdCss: String = if (apiDocs.swagger.displayOperationId) {
                """
                    // Inject custom CSS for operationId styling.
                    const style = document.createElement('style');
                    style.innerHTML = `.opblock-summary-operation-id { word-break: normal !important; }`;
                    document.head.appendChild(style);
                """.trimIndent()
            } else ""

            // Dark theme CSS.
            val swaggerDarkCssUrl: String = if (apiDocs.swagger.uiTheme == SwaggerUiTheme.DARK) {
                """
                    // Inject the dark theme CSS.
                    const darkThemeLink = document.createElement('link');
                    darkThemeLink.rel = 'stylesheet';
                    darkThemeLink.type = 'text/css';
                    darkThemeLink.href = '/static-kopapi/dark-theme.css';
                    document.head.appendChild(darkThemeLink);
                """.trimIndent()
            } else ""

            swaggerJs = """
                window.onload = function() {
                    $operationIdCss
                    $swaggerDarkCssUrl
    
                    // Initialize Swagger UI.
                    window.ui = SwaggerUIBundle({
                        url: "${apiDocs.openApiUrl}",
                        dom_id: '#swagger-ui',
                        deepLinking: true,
                        presets: [
                            SwaggerUIBundle.presets.apis,
                            SwaggerUIStandalonePreset
                        ],
                        plugins: [
                            SwaggerUIBundle.plugins.DownloadUrl
                        ],
                        layout: "StandaloneLayout",
                        filter: true,
                        persistAuthorization: ${apiDocs.swagger.persistAuthorization},
                        withCredentials: ${apiDocs.swagger.withCredentials},
                        displayRequestDuration: ${apiDocs.swagger.displayRequestDuration},
                        displayOperationId: ${apiDocs.swagger.displayOperationId},
                        $sorterSetting
                        $syntaxThemeSetting
                        docExpansion: "${apiDocs.swagger.docExpansion.value}",
                    });
                };
            """.trimIndent()
        }

        return swaggerJs
    }
}
