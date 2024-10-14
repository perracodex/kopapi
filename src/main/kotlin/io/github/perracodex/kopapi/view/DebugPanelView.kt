/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.view

import io.github.perracodex.kopapi.core.composer.SchemaComposer
import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Generates the HTML view of the debug panel, displaying API metadata, object schemas,
 * and schema conflicts in a structured, interactive format.
 *
 * The HTML page is built using Kotlin's HTML DSL, with separate panels for each data type.
 * Each panel includes a dropdown for filtering the displayed JSON data and syntax highlighting.
 */
internal class DebugPanelView {
    private val apiMetadataJson: Set<String> = SchemaComposer.getApiMetadataJson()
    private val schemasJson: Set<String> = SchemaComposer.getSchemasJson()
    private val schemaConflictsJson: Set<String> = SchemaComposer.getSchemaConflictsJson()
    private val json = Json { prettyPrint = true }

    /**
     * Constructs the full debug panel HTML view.
     *
     * @param html The [HTML] DSL builder used to create the view.
     */
    fun build(html: HTML) {
        // Parse JSON strings into JsonObject lists.
        val apiMetadataList: List<JsonObject> = apiMetadataJson.map { Json.parseToJsonElement(it).jsonObject }
        val schemasList: List<JsonObject> = schemasJson.map { Json.parseToJsonElement(it).jsonObject }
        val schemaConflictsList: List<JsonObject> = schemaConflictsJson.map { Json.parseToJsonElement(it).jsonObject }

        with(html) {
            // Build the head of the HTML page.
            head {
                title { +"Kopapi Debug Information" }
                link(rel = "stylesheet", type = "text/css", href = "/static-kopapi/styles/prism.css")
                link(rel = "stylesheet", type = "text/css", href = "/static-kopapi/styles/view.css")
            }
            // Build the body of the HTML page.
            body {
                h1(classes = "header") { +"Kopapi Debug Information" }
                div(classes = "panel-container") {
                    // Build individual panels for each JSON section.
                    buildPanel(
                        htmlTag = this,
                        title = "Routes API Metadata",
                        panelId = "routes-api-metadata",
                        keys = listOf("method", "path"),
                        jsonDataList = apiMetadataList
                    )
                    if (schemasList.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Object Schemas",
                            panelId = "objects-schemas",
                            keys = listOf("name", "type"),
                            jsonDataList = schemasList
                        )
                    }
                    if (schemaConflictsList.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Schema Conflicts",
                            panelId = "schema-conflicts",
                            keys = listOf("name"),
                            jsonDataList = schemaConflictsList
                        )
                    }
                }

                // Include Prism.js for syntax highlighting.
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js") {}
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-json.min.js") {}

                // Include custom JavaScript files for copy-to-clipboard and dropdown filtering.
                script(src = "/static-kopapi/js/copy.js", type = "text/javascript") {}
                script(src = "/static-kopapi/js/selection.js", type = "text/javascript") {}
            }
        }
    }

    /**
     * Constructs an individual panel in the debug view.
     *
     * @param htmlTag The parent HTML element to append the panel to.
     * @param title The title of the panel.
     * @param panelId The unique identifier for the panel element.
     * @param jsonDataList The list of JSON objects to be displayed in the panel.
     * @param keys The keys used for building dropdown options from the JSON objects.
     */
    private fun buildPanel(
        htmlTag: FlowContent,
        title: String,
        panelId: String,
        keys: List<String>,
        jsonDataList: List<JsonObject>
    ) {
        val jsonData: String = json.encodeToString(jsonDataList)

        with(htmlTag) {
            div(classes = "panel") {
                id = panelId

                h2(classes = "panel-title") {
                    // Add the toggle icon on the left.
                    span(classes = "toggle-icon") {
                        onClick = "togglePanel('$panelId')"
                        +"+" // Initially expanded.
                    }

                    +"$title (${jsonDataList.size})"

                    // Add the copy action on the right.
                    span(classes = "copy-action") {
                        onClick = "copyToClipboard('$panelId')"
                        +"copy"
                    }
                }

                // Dropdown for filtering the JSON based on specific keys.
                select(classes = "filter-dropdown") {
                    id = "$panelId-filter"

                    // Add an "All" option to display all JSON data.
                    option {
                        value = "ALL" // This constant must match the filter logic in `selection.js`.
                        attributes["data-full-json"] = jsonData // Store full JSON in a data attribute.
                        +"All"
                    }

                    // Populate dropdown with options, including pretty-printed JSON data.
                    jsonDataList.forEach { jsonObject ->
                        // Build a composite key from the specified keys.
                        val compositeKey: String = keys.joinToString(separator = " → ") { key ->
                            jsonObject[key]?.jsonPrimitive?.content.orEmpty()
                        }
                        option {
                            // Store the pretty-printed JSON object as a value.
                            value = json.encodeToString(jsonObject)
                            // Display the key in the dropdown.
                            +compositeKey
                        }
                    }
                }

                // JSON code content panel.
                pre(classes = "panel-content") {
                    code(classes = "language-json") {
                        // Initial value is the full pretty-printed JSON data.
                        +jsonData
                    }
                }
            }
        }
    }
}
