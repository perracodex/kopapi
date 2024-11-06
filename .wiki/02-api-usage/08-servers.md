## Registering Server Configurations at Operation Level

Servers can also be defined at the operation level, allowing for more specific server configurations.
The syntax is the same as the top-level configuration, but the servers are applied only to the specific operation.

- See also: [Server Configuration](../01-plugin/02-servers.md)

```kotlin
fun Application.configureRoutes() {

    routing {
        get("/some-endpoint") {
            // Implement as usual
        } api {
            //...

            servers {
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
            }
        }
    }
}

```

---

### [Schema Annotation 🡲](09-schema-annotation.md)

#### [🡰 Security](07-security.md)
