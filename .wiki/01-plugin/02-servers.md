## Registering Server Configurations

The plugin allows to register a list of servers to include in the OpenAPI schema.

Optionally the configuration can include variables to define server URLs with placeholders.

```kotlin
install(Kopapi) {

    //...

    servers {
        // Simple example with no variables.
        add(urlString = "http://localhost:8080") {
            description = "Local server for development."
        }

        // Example with variable placeholders.
        add(urlString = "{protocol}://{environment}.example.com:{port}") {
            description = "The server with environment variable."

            // Environment.
            variable(name = "environment", defaultValue = "production") {
                choices = setOf("production", "staging", "development")
                description = "Specifies the environment (production, staging, etc.)."
            }

            // Port.
            variable(name = "port", defaultValue = "8080") {
                choices = setOf("8080", "8443")
                description = "The port for the server."
            }

            // Protocol.
            variable(name = "protocol", defaultValue = "http") {
                choices = setOf("http", "https")
            }
        }

        // Example with region variable.
        add(urlString = "https://{region}.api.example.com") {
            description = "Server for the API by region."

            variable(name = "region", defaultValue = "us") {
                choices = setOf("us", "eu")
                description = "Specifies the region for the API (us, eu)."
            }
        }
    }
}
```

---

### [Security 🡲](03-security.md)

#### [🡰 Plugin Configuration](01-configuration.md) 
