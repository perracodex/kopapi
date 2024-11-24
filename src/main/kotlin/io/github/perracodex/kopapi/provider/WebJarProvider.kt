/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.provider

import io.github.perracodex.kopapi.system.KopapiException
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

/**
 * Provides access to WebJar resources for serving content.
 */
internal object WebJarProvider {
    /**
     * Retrieves the content for the specified file from the webjar resources.
     *
     * It attempts to locate and serve a specific file (e.g., JavaScript, HTML) from a WebJar resources.
     *
     * @param environment The application environment to access resources.
     * @param path The path to the file (e.g., "META-INF/resources/webjars/swagger-ui").
     * @param filename The name of the file to retrieve (e.g., "index.html").
     * @return The outgoing content representing the specified file, or `null` if the file is not found.
     */
    fun getContentFor(environment: ApplicationEnvironment, path: String, filename: String): OutgoingContent? {
        val filePath = "$path/$filename"
        val resource: URL = environment::class.java.classLoader.getResource(filePath) ?: return null
        return createOutgoingContent(resource, filePath)
    }

    /**
     * Retrieves the version from the WebJar's pom.properties.
     *
     * @param environment The application environment to access resources.
     * @param propertiesPath The path to the pom.properties file.
     * @return The version string of WebJar resources.
     * @throws KopapiException If the version information is missing or the WebJar is not found.
     */
    fun getVersion(environment: ApplicationEnvironment, propertiesPath: String): String {
        val propertiesStream: InputStream = environment::class.java.classLoader.getResourceAsStream(propertiesPath)
            ?: throw KopapiException("WebJar not found on the classpath.")

        return propertiesStream.bufferedReader().use { reader ->
            Properties().apply {
                load(reader)
            }.getProperty("version")
                ?: throw KopapiException("WebJar version not specified in pom.properties.")
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
}
