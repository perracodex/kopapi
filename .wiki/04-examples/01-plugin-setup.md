## Example: Configuring the Plugin

```kotlin
fun Application.configureApiSchema() {
    install(plugin = Kopapi) {
        // Enable API documentation
        // Typically should be enabled only in development environment
        enabled = true

        // Define API documentation settings
        apiDocs {
            openapiYamlUrl = "/api/openapi.yaml"
            openapiJsonUrl = "/api/openapi.json"
            redocUrl = "/api/redoc"
            swaggerUrl = "/api/swagger"
            withCredentials = true
            operationsSorter = SwaggerOperationsSorter.METHOD
            swaggerSyntaxTheme = SwaggerSyntaxTheme.NORD
        }

        // Register API info
        info {
            title = "Super API"
            version = "1.0.0"
            description = "Super API Documentation"
            contact {
                name = "Super Kopapi"
                url = "https://example.com/support"
                email = "support@example.com"
            }
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        // Register servers
        servers {
            add(urlString = "{protocol}://{host}:{port}") {
                description = "Super API Server"
                variable(name = "protocol", defaultValue = "http") {
                    choices = setOf("http", "https")
                }
                variable(name = "host", defaultValue = "localhost") {
                    choices = setOf("super-server", "localhost")
                }
                variable(name = "port", defaultValue = "8080") {
                    choices = setOf("08080", "443")
                }
            }
        }

        // Top level security schemes
        bearerSecurity(name = "BearerAuth") {
            description = "HTTP bearer Authentication is required."
        }
        basicSecurity(name = "BasicAuth") {
            description = "HTTP basic Authentication is required."
        }
        oauth2Security(name = "OAuth2") {
            description = "OAuth2 Authentication."
            authorizationCode {
                authorizationUrl = "https://example.com/auth"
                tokenUrl = "https://example.com/token"
                refreshUrl = "https://example.com/refresh"
                scope(name = "read:employees", description = "Read Data")
                scope(name = "write:employees", description = "Modify Data")
            }
            clientCredentials {
                tokenUrl = "https://example.com/token"
                scope(name = "admin:tools", description = "Administrate Tools")
            }
        }

        // Top level tags
        tags {
            add(name = "RBAC", description = "Role-Based Access Control")
            add(name = "System", description = "System Management")
            add(name = "Employee", description = "Employee Management")
        }
    }
}

```