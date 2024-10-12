/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.core.SchemaProvider
import kotlinx.html.*

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
        val apiMetadataJson: String = SchemaProvider.getApiMetadataJson()
        val schemasJson: String = SchemaProvider.getSchemasJson()
        val schemaConflictsJson: String = SchemaProvider.getSchemaConflictsJson()

        with(html) {
            head {
                title { +"Kopapi Debug Information" }
                link(rel = "stylesheet", type = "text/css", href = "/static-kopapi/prism.css")
                link(rel = "stylesheet", type = "text/css", href = "/static-kopapi/debug.css")
            }
            body {
                h1(classes = "header") { +"Kopapi Debug Information" }
                div(classes = "panel-container") {
                    // Panel 1: Routes Metadata.
                    div(classes = "panel") {
                        h2(classes = "panel-title") {
                            +"Routes Metadata"
                            span(classes = "copy-icon") {
                                onClick = "copyToClipboard('routes-metadata')"
                                +"📋"
                            }
                        }
                        pre(classes = "panel-content") {
                            id = "routes-metadata"
                            code(classes = "language-json") {
                                +apiMetadataJson
                            }
                        }
                    }
                    // Panel 2: Object Schemas.
                    div(classes = "panel") {
                        h2(classes = "panel-title") {
                            +"Object Schemas"
                            span(classes = "copy-icon") {
                                onClick = "copyToClipboard('objects-schemas')"
                                +"📋"
                            }
                        }
                        pre(classes = "panel-content") {
                            id = "objects-schemas"
                            code(classes = "language-json") {
                                +schemasJson
                            }
                        }
                    }
                    // Panel 3: Schema Conflicts.
                    div(classes = "panel") {
                        h2(classes = "panel-title") {
                            +"Schema Conflicts"
                            span(classes = "copy-icon") {
                                onClick = "copyToClipboard('schema-conflicts')"
                                +"📋"
                            }
                        }
                        pre(classes = "panel-content") {
                            id = "schema-conflicts"
                            code(classes = "language-json") {
                                +schemaConflictsJson
                            }
                        }
                    }
                }

                // Core Prism.js library, which provides the main functionality for syntax highlighting.
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js") {}
                // Prism.js language component for JSON. It adds support for JSON syntax highlighting to the core library.
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-json.min.js") {}

                // JavaScript to handle copying to clipboard.
                script {
                    unsafe {
                        +"""
                        function copyToClipboard(elementId) {
                            const element = document.getElementById(elementId);
                            const range = document.createRange();
                            range.selectNodeContents(element);
                            const selection = window.getSelection();
                            selection.removeAllRanges();
                            selection.addRange(range);
                            document.execCommand('copy');
                            window.getSelection().removeAllRanges();
                            alert('Content copied to clipboard');
                        }
                        """.trimIndent()
                    }
                }
            }
        }
    }
}
