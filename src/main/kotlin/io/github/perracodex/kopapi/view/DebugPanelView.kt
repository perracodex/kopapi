// DebugPanelView.kt

package io.github.perracodex.kopapi.view

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
internal class DebugPanelView {
    private val apiMetadataJson: List<String> = SchemaProvider.getApiMetadataJson()
    private val schemasJson: List<String> = SchemaProvider.getSchemasJson()
    private val schemaConflictsJson: List<String> = SchemaProvider.getSchemaConflictsJson()
    private val json = Json { prettyPrint = true }

    /**
     * Builds the debug panel view.
     *
     * @param html The HTML DSL builder used to construct the view.
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
            // Build the body of the HTML page
            body {
                h1(classes = "header") { +"Kopapi Debug Information" }
                div(classes = "panel-container") {
                    // Build individual panels for each JSON section.
                    buildPanel(
                        htmlTag = this,
                        title = "Routes API Metadata",
                        panelId = "routes-api-metadata",
                        jsonDataList = apiMetadataList,
                        keys = listOf("method", "path")
                    )
                    if (schemasList.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Object Schemas",
                            panelId = "objects-schemas",
                            jsonDataList = schemasList,
                            keys = listOf("name")
                        )
                    }
                    if (schemaConflictsList.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Schema Conflicts",
                            panelId = "schema-conflicts",
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
     * @param jsonDataList The list of JSON objects used for the dropdown.
     * @param keys The keys used for populating the dropdown from JSON (e.g., 'path', 'method').
     */
    private fun buildPanel(
        htmlTag: FlowContent,
        title: String,
        panelId: String,
        jsonDataList: List<JsonObject>,
        keys: List<String>
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
                    // Add the copy icon on the right.
                    span(classes = "copy-icon") {
                        onClick = "copyToClipboard('$panelId')"
                        +"📋" // Copy icon.
                    }
                }
                // Dropdown for filtering the JSON based on specific keys.
                select(classes = "filter-dropdown") {
                    id = "$panelId-filter"

                    // Add an "All" option to display all JSON data.
                    option {
                        value = "ALL" // Must match the filter logic in selection.js.
                        attributes["data-full-json"] = jsonData // Store full JSON in a data attribute.
                        +"All"
                    }

                    // Populate dropdown with options, including pretty-printed JSON data.
                    jsonDataList.forEach { jsonObject ->
                        // Build a composite key from the specified keys.
                        val compositeKey: String = keys.joinToString(separator = " | ") { key ->
                            jsonObject[key]?.jsonPrimitive?.content.orEmpty()
                        }
                        option {
                            value = json.encodeToString(jsonObject) // Store the pretty-printed JSON object as a value.
                            +compositeKey // Display the key in the dropdown.
                        }
                    }
                }
                // JSON content panel.
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
