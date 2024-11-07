/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.SwaggerOperationsSorter
import io.github.perracodex.kopapi.types.SwaggerSyntaxTheme
import io.github.perracodex.kopapi.types.SwaggerUiTheme
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

/**
 * Utility object for handling Swagger UI resources providing functionality
 * to retrieve and serve Swagger UI files from the WebJar package.
 */
internal object Swagger {
    /** Path to the Swagger UI resources within the WebJar. */
    private const val WEBJARS_PATH: String = "META-INF/resources/webjars/swagger-ui/"

    /** Path to the properties file that holds the Swagger UI version information. */
    private const val PROPERTIES_PATH: String = "META-INF/maven/org.webjars/swagger-ui/pom.properties"

    /** Cached path to the Swagger UI resources within the WebJar. */
    private lateinit var swaggerPath: String

    /** The  JavaScript needed to initialize the Swagger UI. */
    private lateinit var swaggerJs: String

    /** The HTML needed to serve the Redoc UI. */
    private lateinit var redocHtml: String

    /**
     * Retrieves the content for the specified file from the Swagger UI resources.
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
            val swaggerUiVersion: String = getVersion(environment = environment)
            swaggerPath = "$WEBJARS_PATH$swaggerUiVersion"
        }

        val filePath = "$swaggerPath/$filename"
        val resource: URL = environment::class.java.classLoader.getResource(filePath) ?: return null
        return createOutgoingContent(resource, filePath)
    }

    /**
     * Retrieves the Swagger UI version from the WebJar's pom.properties.
     *
     * @param environment The application environment to access resources.
     * @return The version string of Swagger UI.
     * @throws KopapiException If the version information is missing or the WebJar is not found.
     */
    private fun getVersion(environment: ApplicationEnvironment): String {
        val propertiesStream: InputStream = environment::class.java.classLoader.getResourceAsStream(PROPERTIES_PATH)
            ?: throw KopapiException("Swagger UI WebJar not found on the classpath.")

        return propertiesStream.bufferedReader().use { reader ->
            Properties().apply {
                load(reader)
            }.getProperty("version")
                ?: throw KopapiException("Swagger UI version not specified in pom.properties.")
        }
    }

    /**
     * Creates outgoing content to serve the specified resource.
     *
     * Converts the resource located by its URL into an `OutgoingContent.ReadChannelContent` which can be used to
     * serve the content as a response in Ktor. This method handles different content types based on the file extension.
     *
     * @param resource The URL of the resource to serve.
     * @param path The path to the file being served.
     * @return An `OutgoingContent.ReadChannelContent` if the resource is valid, otherwise `null`.
     */
    private fun createOutgoingContent(resource: URL, path: String): OutgoingContent.ReadChannelContent? {
        val jarFile: File = findContainingJarFile(url = resource.toString())
        val extension: String = path.extension()
        val contentType: ContentType = ContentType.defaultForFileExtension(extension)
        return JarFileContent(
            jarFile = jarFile,
            resourcePath = path,
            contentType = contentType
        ).takeIf { it.isFile }
    }

    /**
     * Finds the containing JAR file for a given resource URL.
     *
     * This is used to locate the JAR file from which the resource originated, provided the resource
     * is part of a local JAR (`jar:file:`). This is necessary for accessing resources packed in JARs.
     *
     * @param url The URL of the resource.
     * @return A `File` representing the JAR containing the resource.
     * @throws KopapiException If the resource is not in a local JAR format.
     */
    private fun findContainingJarFile(url: String): File {
        if (url.startsWith(prefix = "jar:file:")) {
            val jarPathSeparator: Int = url.indexOf(string = "!", startIndex = 9)
            require(value = jarPathSeparator != -1) { "Jar path requires !/ separator but it is: $url" }
            return File(url.substring(startIndex = 9, endIndex = jarPathSeparator).decodeURLPart())
        }
        throw KopapiException("Only local jars are supported (jar:file:)")
    }

    /**
     * Extension function to extract the file extension from a path
     * to determine the type of resource, which is useful for setting
     * the correct `Content-Type` header when serving resources.
     *
     * @return The file extension (including the dot), or an empty string if there is no extension.
     */
    private fun String.extension(): String {
        val indexOfName: Int = lastIndexOf(char = '/').takeIf { it != -1 }
            ?: lastIndexOf(char = '\\').takeIf { it != -1 } ?: 0
        val indexOfDot: Int = indexOf(char = '.', indexOfName)
        return when (indexOfDot >= 0) {
            true -> substring(startIndex = indexOfDot)
            false -> ""
        }
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
                        url: "${apiDocs.openapiYamlUrl}",
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
                    });
                };
            """.trimIndent()
        }

        return swaggerJs
    }

    /**
     * Returns the HTML needed to serve the Redoc UI.
     *
     * @param openapiYamlUrl The URL to access the OpenAPI schema in YAML format.
     * @return The HTML code to serve the Redoc UI.
     */
    fun getRedocHtml(openapiYamlUrl: String): String {
        if (!this::redocHtml.isInitialized) {
            redocHtml = """
                <!DOCTYPE html>
                <html lang="en">
                    <head>
                        <title>ReDoc API Schema</title>
                        <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
                    </head>
                    <body>
                        <redoc spec-url='$openapiYamlUrl'></redoc>
                        <script>
                            Redoc.init('$openapiYamlUrl');
                        </script>
                    </body>
                </html>
            """.trimIndent()
        }

        return redocHtml
    }
}
