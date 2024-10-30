/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.view

import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.view.annotation.DebugViewAPI
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
@DebugViewAPI
internal class DebugPanelView(private val debugInfo: DebugInfo) {
    // Keep existing SchemaRegistry usages for conflicts and top action buttons
    private val schemaConflictsJson: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.SCHEMA_CONFLICTS)
    private val configurationJson: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.API_CONFIGURATION)
    private val openApiYaml: String = SchemaRegistry.getOpenApiSchema(format = SchemaRegistry.Format.YAML)
    private val openApiJson: String = SchemaRegistry.getOpenApiSchema(format = SchemaRegistry.Format.JSON)
    private val jsonParser: Json = Json { prettyPrint = true }

    /**
     * Constructs the full debug panel HTML view.
     *
     * @param html The [HTML] DSL builder used to create the view.
     */
    fun build(html: HTML) {
        // Use DebugInfo for API Operations and Type Schemas panels
        val apiOperationSections: Map<String, DebugInfo.Section> = debugInfo.apiOperationSections
        val typeSchemaSections: Map<String, DebugInfo.Section> = debugInfo.typeSchemaSections

        // Parse JSON strings into JsonObject lists for conflicts panel
        val schemaConflictsList: List<JsonObject> = schemaConflictsJson.map { Json.parseToJsonElement(it).jsonObject }

        with(html) {
            // Build the head of the HTML page.
            head {
                title { +"Kopapi Debug Information" }
                link(rel = "stylesheet", type = "text/css", href = "/static-kopapi/styles/view.css")
                link(rel = "stylesheet", type = "text/css", href = "/static-kopapi/styles/prism.css")

                // Include Prism.js for syntax highlighting.
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js") {}
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-json.min.js") {}
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-yaml.min.js") {}

                // Include custom JavaScript files for the debug view.
                script(src = "/static-kopapi/js/copy.js", type = "text/javascript") {}
                script(src = "/static-kopapi/js/selection.js", type = "text/javascript") {}
                script(src = "/static-kopapi/js/popup.js", type = "text/javascript") {}
                script(src = "/static-kopapi/js/format.js", type = "text/javascript") {}
                script(src = "/static-kopapi/js/toggle.js", type = "text/javascript") {}
            }
            // Build the body of the HTML page.
            body {
                buildActionButtons(htmlTag = this)

                div(classes = "panel-container") {
                    // Build individual panels for each JSON section.
                    buildPanel(
                        htmlTag = this,
                        title = "Operations",
                        panelId = "api-operation",
                        sections = apiOperationSections,
                        allRawData = apiOperationSections.values.joinToString("\n") { it.rawSection },
                        allYamlData = debugInfo.allApiOperationsYamlSection,
                        allJsonData = debugInfo.allApiOperationsJsonSection
                    )
                    if (typeSchemaSections.isNotEmpty()) {
                        buildPanel(
                            htmlTag = this,
                            title = "Schemas",
                            panelId = "type-schemas",
                            sections = typeSchemaSections,
                            allRawData = typeSchemaSections.values.joinToString("\n") { it.rawSection },
                            allYamlData = debugInfo.allTypeSchemasYamlSection,
                            allJsonData = debugInfo.allTypeSchemasJsonSection
                        )
                    }
                    if (schemaConflictsList.isNotEmpty()) {
                        buildConflictsPanel(
                            htmlTag = this,
                            keys = listOf("name"),
                            jsonDataList = schemaConflictsList
                        )
                    }
                }

                // Popups.
                buildPopup(
                    htmlTag = this,
                    panelId = "configuration-panel",
                    title = "Kopapi Configuration",
                    content = configurationJson.first()
                )
                buildPopup(
                    htmlTag = this,
                    panelId = "openapi-yaml-panel",
                    title = "YAML OpenAPI Schema",
                    content = openApiYaml
                )
                buildPopup(
                    htmlTag = this,
                    panelId = "openapi-json-panel",
                    title = "JSON OpenAPI Schema",
                    content = openApiJson
                )
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
                    +"Yaml Schema"
                }

                button(classes = "action-button") {
                    id = "openapi-json-panel-button"
                    onClick = "showPopup('openapi-json-panel')"
                    +"Json Schema"
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
     * @param sections The map of composite keys to their corresponding sections.
     * @param allRawData The full raw data for the "All" option.
     * @param allYamlData The combined YAML data for the "All" option.
     * @param allJsonData The combined JSON data for the "All" option.
     */
    private fun buildPanel(
        htmlTag: FlowContent,
        title: String,
        panelId: String,
        sections: Map<String, DebugInfo.Section>,
        allRawData: String,
        allYamlData: String,
        allJsonData: String
    ) {
        with(htmlTag) {
            div(classes = "panel") {
                id = panelId

                h2(classes = "panel-title") {
                    div(classes = "panel-title-text") {
                        +"$title: ${sections.size}"
                    }

                    span(classes = "panel-action raw-button") {
                        onClick = "switchContent('$panelId', 'raw')"
                        +"raw"
                    }
                    span(classes = "panel-action yaml-button") {
                        onClick = "switchContent('$panelId', 'yaml')"
                        +"yaml"
                    }
                    span(classes = "panel-action json-button") {
                        onClick = "switchContent('$panelId', 'json')"
                        +"json"
                    }

                    span(classes = "copy-action") {
                        onClick = "copyToClipboard('$panelId')"
                        +"copy"
                    }

                    // The expand/collapse icon.
                    span(classes = "toggle-icon") {
                        onClick = "togglePanel('$panelId')"
                        +"+" // Initially expanded.
                    }
                }

                // Dropdown for filtering based on specific keys.
                select(classes = "filter-dropdown") {
                    id = "$panelId-data-filter"

                    // Add an "All" option to display all data.
                    option {
                        value = "ALL"
                        attributes["data-raw"] = allRawData
                        attributes["data-yaml"] = allYamlData
                        attributes["data-json"] = allJsonData
                        +"All"
                    }

                    // Populate dropdown with options.
                    sections.forEach { (compositeKey, section) ->
                        option {
                            value = compositeKey
                            attributes["data-raw"] = section.rawSection
                            attributes["data-yaml"] = section.yamlSection
                            attributes["data-json"] = section.jsonSection
                            +compositeKey
                        }
                    }
                }

                // Content panel.
                pre(classes = "panel-content") {
                    code(classes = "language-yaml") {
                        +allYamlData // Default to YAML data.
                    }
                }
            }
        }
    }

    /**
     * Constructs an individual panel in the debug view for conflicts.
     *
     * @param htmlTag The parent HTML element to append the panel to.
     * @param keys The keys used for building dropdown options from the JSON objects.
     * @param jsonDataList The list of JSON objects to be displayed in the panel.
     */
    private fun buildConflictsPanel(
        htmlTag: FlowContent,
        keys: List<String>,
        jsonDataList: List<JsonObject>
    ) {
        val title = "Schema Conflicts"
        val panelId = "type-schema-conflicts"
        val jsonData: String = jsonParser.encodeToString(jsonDataList)
        val filterId = "$panelId-data-filter"

        with(htmlTag) {
            div(classes = "panel") {
                id = panelId

                h2(classes = "panel-title") {
                    // Title in the center.
                    div(classes = "panel-title-text") {
                        +"$title: ${jsonDataList.size}"
                    }

                    span(classes = "copy-action") {
                        onClick = "copyToClipboard('$panelId')"
                        +"copy"
                    }

                    // The expand/collapse icon.
                    span(classes = "toggle-icon") {
                        onClick = "togglePanel('$panelId')"
                        +"+" // Initially expanded.
                    }
                }

                // Dropdown for filtering the JSON based on specific keys.
                select(classes = "filter-dropdown") {
                    id = filterId

                    // Add an "All" option to display all JSON data.
                    option {
                        value = "ALL" // This constant must match the filter logic in `selection.js`.
                        attributes["data-json"] = jsonData
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
                            value = jsonParser.encodeToString(jsonObject)
                            attributes["data-json"] = jsonParser.encodeToString(jsonObject)
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
     * @param content The content to be displayed in the popup.
     */
    private fun buildPopup(
        htmlTag: FlowContent,
        panelId: String,
        title: String,
        content: String
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
                                +content
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
