/*
 * Copyright (c) 2024-Present Perracodex.
 * Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.core.SchemaProvider
import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Handles the generation of the debug panel view.
 */
internal object DebugPanelView {
    /**
     * Builds the debug panel view.
     *
     * @param html The HTML DSL builder used to construct the view.
     */
    fun build(html: HTML) {
        // Fetch JSON data from SchemaProvider.
        val apiMetadataJson: List<String> = SchemaProvider.getApiMetadataJson()
        val schemasJson: List<String> = SchemaProvider.getSchemasJson()
        val schemaConflictsJson: List<String> = SchemaProvider.getSchemaConflictsJson()

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
            // Build the body of the HTML page
            body {
                h1(classes = "header") { +"Kopapi Debug Information" }
                div(classes = "panel-container") {
                    // Build individual panels for each JSON section.
                    buildPanel(
                        htmlTag = this,
                        title = "Routes API Metadata",
                        panelId = "routes-api-metadata",
                        jsonData = Json.encodeToString(apiMetadataList),
                        jsonDataList = apiMetadataList,
                        keys = listOf("method", "path")
                    )
                    if (schemasList.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Object Schemas",
                            panelId = "objects-schemas",
                            jsonData = Json.encodeToString(schemasList),
                            jsonDataList = schemasList,
                            keys = listOf("name")
                        )
                    }
                    if (schemaConflictsList.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Schema Conflicts",
                            panelId = "schema-conflicts",
                            jsonData = Json.encodeToString(schemaConflictsList),
                            jsonDataList = schemaConflictsList,
                            keys = listOf("name")
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
     * A common method to build each panel.
     *
     * @param htmlTag The HTML tag used for building the panel.
     * @param title The title of the panel.
     * @param panelId The panel content ID.
     * @param jsonData The JSON data to display in the panel.
     * @param jsonDataList The list of JSON objects used for the dropdown.
     * @param keys The keys used for populating the dropdown from JSON (e.g., 'path', 'method').
     */
    private fun buildPanel(
        htmlTag: FlowContent,
        title: String,
        panelId: String,
        jsonData: String,
        jsonDataList: List<JsonObject>,
        keys: List<String>
    ) {
        with(htmlTag) {
            div(classes = "panel") {
                h2(classes = "panel-title") {
                    +"$title (${jsonDataList.size})"
                    span(classes = "copy-icon") {
                        onClick = "copyToClipboard('$panelId')"
                        +"📋" // Copy icon.
                    }
                }
                // Dropdown for filtering the JSON based on specific keys
                select(classes = "filter-dropdown") {
                    id = "$panelId-filter"

                    // Add an "All" option to display all JSON data.
                    option {
                        value = jsonData // Store the full JSON data as value for "All".
                        +"All"
                    }

                    // Populate dropdown with options, including hidden JSON data.
                    jsonDataList.forEach { jsonObject ->
                        // Build a composite key from the specified keys.
                        val compositeKey: String = keys.joinToString(separator = " | ") { key ->
                            jsonObject[key]?.jsonPrimitive?.content.orEmpty()
                        }
                        option {
                            value = jsonObject.toString() // Store the full JSON object as a value
                            +compositeKey // Display the key in the dropdown
                        }
                    }
                }
                // JSON content panel.
                pre(classes = "panel-content") {
                    id = panelId
                    code(classes = "language-json") {
                        // Initial value is the full JSON data.
                        // This is replaced by the selected JSON object when the dropdown changes.
                        val json = Json { prettyPrint = true }
                        val prettyPrintedJson: String = json.encodeToString(Json.parseToJsonElement(jsonData))
                        +prettyPrintedJson
                    }
                }
            }
        }
    }
}
