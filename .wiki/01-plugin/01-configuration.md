## Plugin Configuration

### Configuration Properties

The `KopapiConfig` provides several properties to define the URLs for your OpenAPI schemas, Swagger UI, and debugging endpoints.

| Property | Description                                                                           | Default          |
|----------|---------------------------------------------------------------------------------------|------------------|
| enabled  | The plugin enabled state.<br/>Useful to disable the plugin for specific environments. | `true`           |
| debugUrl | The URL to provide the API metadata for debugging purposes.                           | `/openapi/debug` |
| apiDocs  | The OpenAPI related URLs and settings, including swagger-ui and redoc.                | N/A              |
| info     | Metadata about your API (title, description, version, contact, license, etc.).        | N/A              |
| servers  | List of servers to include in the OpenAPI schema, with optional URL variables.        | N/A              |
| security | List of top-level security schemes, overriding per endpoint schemes.                  | N/A              |

- The `debugUrl` endpoint provides access to vital diagnostic information that assists in troubleshooting the plugin behavior.
  If the schema is not generated as expected, you can use this endpoint to inspect and identify potential issues.

---

### API Documentation Section

| Property   | Description                                                                    | Default                             |
|------------|--------------------------------------------------------------------------------|-------------------------------------|
| openapiUrl | The URL to provide the OpenAPI schema file, in both `yaml` and `json` formats. | `/openapi.yaml`<br/>`/openapi.json` |
| redocUrl   | The URL to provide access to the OpenAPI `Redoc` documentation.                | `/redoc`                            |

```kotlin
install(Kopapi) {
    apiDocs {
        openapiUrl = "/api/"
        redocUrl = "/api/redoc"
    }
}
```

- Paths are relative URL path for the OpenAPI schema.

The `openapiUrl` provides access to the OpenAPI schema in both `yaml` and `json` formats.
You can provide just a path or a file path with a format extension. The alternative format will be appended automatically.
When defining a filename, it must have either a `.yaml` or `.json` extension, otherwise will be treated as a directory path.

- Directory Path
    - Will append `openapi.yaml` and `openapi.json`.
        - *Example:* `"/api/"`
        - *Results:* `"/api/openapi.yaml"`, `"/api/openapi.json"`

- File Path with Format Extension (`.yaml` or `.json`)
    - Retains the provided file and adds the alternate format extension.
        - *Example:* `"/api/spec.yaml"`
        - *Results:* `"/api/spec.yaml"`, `"/api/spec.json"`

---

### Swagger Section

| Property               | Description                                                                                | Default        |
|------------------------|--------------------------------------------------------------------------------------------|----------------|
| url                    | The URL to provide access to the `Swagger UI`.                                             | `/swagger-ui/` |
| persistAuthorization   | Whether to persist entered authorizations in `Swagger UI` to retain them on page refresh.  | `false`        |
| withCredentials        | Whether to include cookies or other credentials in CORS requests from `Swagger UI`.        | `false`        |
| docExpansion           | The default expansion for the documentation in the `Swagger UI`.                           | `LIST`         |
| displayRequestDuration | Whether to display the request duration in `Swagger UI`.                                   | `false`        |
| displayOperationId     | Whether to display the endpoint `operationId` in `Swagger UI`.                             | `false`        |
| operationsSorter       | The sorter to use for the operations in the `Swagger UI`.                                  | `UNSORTED`     |
| uiTheme                | The theme to use for the overall Swagger UI.                                               | `LIGHT`        |
| syntaxTheme            | The syntax highlighting theme to use for the `Swagger UI`.                                 | `AGATE`        |
| includeErrors          | When enabled, detected generation errors will be appended into `info` section description. | `false`        |

```kotlin
install(Kopapi) {
    apiDocs {
        // ...

        // Swagger UI settings.
        swagger {
            url = "/swagger-ui/"
            persistAuthorization = true
            withCredentials = true
            docExpansion = SwaggerDocExpansion.NONE
            displayRequestDuration = true
            displayOperationId = true
            operationsSorter = SwaggerOperationsSorter.METHOD
            uiTheme = SwaggerUITheme.DARK
            syntaxTheme = SwaggerSyntaxTheme.AGATE
            includeErrors = true
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
    debugUrl = "openapi/debug"

    // API Documentation section.
    apiDocs {
        openapilUrl = "/api/"
        redocUrl = "/api/redoc"

        // Swagger UI settings.
        swagger {
            url = "/swagger-ui/"
            persistAuthorization = true
            withCredentials = true
            docExpansion = SwaggerDocExpansion.NONE
            displayRequestDuration = true
            displayOperationId = true
            operationsSorter = SwaggerOperationsSorter.METHOD
            uiTheme = SwaggerUITheme.DARK
            syntaxTheme = SwaggerSyntaxTheme.AGATE
            includeErrors = true
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
