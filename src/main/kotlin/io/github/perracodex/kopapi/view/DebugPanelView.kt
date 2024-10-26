/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.view

import io.github.perracodex.kopapi.composer.SchemaRegistry
import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/**
 * Generates the HTML view of the debug panel, displaying API metadata, object schemas,
 * and schema conflicts in a structured, interactive format.
 *
 * The HTML page is built using Kotlin's HTML DSL, with separate panels for each data type.
 * Each panel includes a dropdown for filtering the displayed JSON data and syntax highlighting.
 */
internal class DebugPanelView {
    private val apiOperationJson: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.API_OPERATION)
    private val typeSchemasJson: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.TYPE_SCHEMAS)
    private val schemaConflictsJson: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.SCHEMA_CONFLICTS)
    private val configurationJson: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.API_CONFIGURATION)
    private val openApiYaml: String = SchemaRegistry.getOpenApiSchema(format = SchemaRegistry.Format.YAML)
    private val openApiJson: String = SchemaRegistry.getOpenApiSchema(format = SchemaRegistry.Format.JSON)

    private val json = Json { prettyPrint = true }

    /**
     * Constructs the full debug panel HTML view.
     *
     * @param html The [HTML] DSL builder used to create the view.
     */
    fun build(html: HTML) {
        // Parse JSON strings into JsonObject lists.
        val apiOperationList: List<JsonObject> = apiOperationJson.map { Json.parseToJsonElement(it).jsonObject }
        val typeSchemasList: List<JsonObject> = typeSchemasJson.map { Json.parseToJsonElement(it).jsonObject }
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
                buildActionButtons(htmlTag = this)

                h1(classes = "header") { +"Kopapi Debug Information" }

                div(classes = "panel-container") {
                    // Build individual panels for each JSON section.
                    buildPanel(
                        htmlTag = this,
                        title = "API Operations",
                        panelId = "api-operation",
                        keys = listOf("operationId", "method", "path"),
                        jsonDataList = apiOperationList
                    )
                    if (typeSchemasList.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Type Schemas",
                            panelId = "type-schemas",
                            keys = listOf("name", "type"),
                            jsonDataList = typeSchemasList
                        )
                    }
                    if (schemaConflictsList.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Type Schema Conflicts",
                            panelId = "type-schema-conflicts",
                            keys = listOf("name"),
                            jsonDataList = schemaConflictsList
                        )
                    }
                }

                // Popups.
                buildPopup(
                    htmlTag = this,
                    panelId = "configuration-panel",
                    title = "Configuration",
                    jsonString = configurationJson.first()
                )
                buildPopup(
                    htmlTag = this,
                    panelId = "openapi-yaml-panel",
                    title = "YAML",
                    jsonString = openApiYaml
                )
                buildPopup(
                    htmlTag = this,
                    panelId = "openapi-json-panel",
                    title = "JSON",
                    jsonString = openApiJson
                )

                // Include Prism.js for syntax highlighting.
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js") {}
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-json.min.js") {}

                // Include custom JavaScript files for the debug view.
                script(src = "/static-kopapi/js/copy.js", type = "text/javascript") {}
                script(src = "/static-kopapi/js/selection.js", type = "text/javascript") {}
                script(src = "/static-kopapi/js/popup.js", type = "text/javascript") {}
            }
        }
    }

    /**
     * Constructs the action buttons in the debug view.
     *
     * @param htmlTag The parent HTML element to append the buttons to.
     */
    private fun buildActionButtons(htmlTag: FlowContent) {
        val redocUrl: String = SchemaRegistry.getResourceUrl(url = SchemaRegistry.ResourceUrl.REDOC)
        val swaggerUrl: String = SchemaRegistry.getResourceUrl(url = SchemaRegistry.ResourceUrl.SWAGGER_UI)

        with(htmlTag) {
            div(classes = "button-container") {
                button(classes = "action-button") {
                    id = "configuration-panel-button"
                    onClick = "showPopup('configuration-panel')"
                    +"Configuration"
                }

                button(classes = "action-button") {
                    id = "openapi-yaml-panel-button"
                    onClick = "showPopup('openapi-yaml-panel')"
                    +"Yaml"
                }

                button(classes = "action-button") {
                    id = "openapi-json-panel-button"
                    onClick = "showPopup('openapi-json-panel')"
                    +"Json"
                }

                button(classes = "action-button") {
                    id = "swagger-ui-button"
                    onClick = "window.open('$swaggerUrl', '_blank')"
                    +"Swagger UI"
                }

                button(classes = "action-button") {
                    id = "redoc-button"
                    onClick = "window.open('$redocUrl', '_blank')"
                    +"ReDoc"
                }
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
        val filterId = "$panelId-data-filter"

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
                    id = filterId

                    // Add an "All" option to display all JSON data.
                    option {
                        value = "ALL" // This constant must match the filter logic in `selection.js`.
                        attributes["data-full-json"] = jsonData // Store full JSON in a data attribute.
                        +"All"
                    }

                    // Populate dropdown with options, including pretty-printed JSON data.
                    jsonDataList.forEach { jsonObject ->
                        // Build a composite key from the specified keys.
                        val compositeKey: String = keys.mapNotNull { key ->
                            jsonObject[key]?.takeIf { it !is JsonNull }
                                ?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                        }.joinToString(separator = " → ")

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

    /**
     * Constructs a popup in the debug view.
     *
     * @param htmlTag The parent HTML element to append the popup to.
     * @param panelId The unique identifier for the panel element.
     * @param title The title of the popup.
     * @param jsonString The JSON object to be displayed in the popup.
     */
    private fun buildPopup(
        htmlTag: FlowContent,
        panelId: String,
        title: String,
        jsonString: String
    ) {
        val overlayId = "${panelId}-overlay"
        val contentId = "${panelId}-content"

        with(htmlTag) {
            div(classes = "popup-overlay") {
                id = overlayId

                div(classes = "popup-content") {
                    id = contentId

                    div(classes = "panel") {
                        id = panelId

                        div(classes = "panel-title") {
                            span { +title }

                            span(classes = "copy-action") {
                                onClick = "copyToClipboard('$panelId')"
                                +"copy"
                            }
                        }

                        pre(classes = "panel-content") {
                            code(classes = "language-json") {
                                +jsonString
                            }
                        }

                        button(classes = "close-popup") {
                            onClick = "hidePopup('$panelId')"
                            +"Close"
                        }
                    }
                }
            }
        }
    }
}
