## Plugin Configuration

### Configuration Properties

The `KopapiConfig` provides several properties to define the URLs for your OpenAPI schemas, Swagger UI, and debugging endpoints.

| Property | Description                                                                           | Default        |
|----------|---------------------------------------------------------------------------------------|----------------|
| enabled  | The plugin enabled state.<br/>Useful to disable the plugin for specific environments. | True           |
| debugUrl | The URL to provide the API metadata for debugging purposes.                           | /openapi/debug |
| apiDocs  | The OpenAPI related URLs and settings, including swagger-ui and redoc.                | N/A            |
| info     | Metadata about your API (title, description, version, contact, license, etc.).        | N/A            |
| servers  | List of servers to include in the OpenAPI schema, with optional URL variables.        | N/A            |
| security | List of global security schemes, overriding per endpoint schemes.                     | N/A            |

- The `debugUrl` endpoint provides access to vital diagnostic information that assists in troubleshooting the plugin behavior.
  If the schema is not generated as expected, you can use this endpoint to inspect and identify potential issues.

---

### API Documentation Section

| Property       | Description                                                     | Default       |
|----------------|-----------------------------------------------------------------|---------------|
| openapiYamlUrl | The URL to provide the OpenAPI schema in `YAML` format.         | /openapi/yaml |
| openapiJsonUrl | The URL to provide the OpenAPI schema in `JSON` format.         | /openapi/json |
| redocUrl       | The URL to provide access to the OpenAPI `Redoc` documentation. | /redoc        |

```kotlin
install(Kopapi) {
    apiDocs {
        openapiYamlUrl = "api/openapi.yaml"
        openapiJsonUrl = "api/openapi.json"
        redocUrl = "api/redoc"
    }
}
```

---

### Swagger Section

| Property         | Description                                                                                        | Default  |
|------------------|----------------------------------------------------------------------------------------------------|----------|
| url              | The URL to provide access to the `Swagger UI`.                                                     | /swagger |
| withCredentials  | Whether to include cookies or other credentials in cross-origin (CORS) requests from `Swagger UI`. | False    |
| operationsSorter | The sorter to use for the operations in the Swagger UI.                                            | UNSORTED |
| syntaxTheme      | The syntax highlighting theme to use for the `Swagger UI`.                                         | AGATE    |

```kotlin
install(Kopapi) {
  apiDocs {
    // ...

    // Swagger UI settings.
    swagger {
      url = "api/swagger"
      withCredentials = true
      operationsSorter = SwaggerOperationsSorter.METHOD
      syntaxTheme = SwaggerSyntaxTheme.AGATE
    }
  }
}
```

---

### API Info Section

The `Info` section can be used to provide metadata about your API .

```kotlin
install(Kopapi) {
    info {
        title = "API Title"
        description = "Api Description"
        version = "1.0.0"
        termsOfService = "https://example.com/terms"

        contact {
            name = "John Doe"
            url = "https://example.com/support"
            email = "support@example.com"
        }

        license {
            name = "MIT"
            url = "https://opensource.org/licenses/MIT"
        }
    }
}
```

---

### Example

```kotlin
install(Kopapi) {
    enabled = true
    debugUrl = "api/debug"

    // API Documentation section.
    apiDocs {
        openapiYamlUrl = "api/openapi.yaml"
        openapiJsonUrl = "api/openapi.json"
        redocUrl = "api/redoc"

      // Swagger UI settings.
      swagger {
        url = "api/swagger"
        withCredentials = true
        operationsSorter = SwaggerOperationsSorter.METHOD
        syntaxTheme = SwaggerSyntaxTheme.AGATE
      }
    }

    // API Info section.
    info {
        title = "API Title"
        description = "Api Description"
        description = "This is a multi-line description."
        version = "1.0.0"
        termsOfService = "https://example.com/terms"

        contact {
            name = "John Doe"
            url = "https://example.com/support"
            email = "support@example.com"
        }

        license {
            name = "MIT"
            url = "https://opensource.org/licenses/MIT"
        }
    }
}
```

--- 

### [Servers 🡲](02-servers.md)
