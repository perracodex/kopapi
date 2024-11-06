## Path-Level Metadata

Metadata can be set at path-level, which will be shared by all operations in that path.
Currently, can be set the following metadata:

- **summary**: A brief summary of the path.
- **description**: A human-readable description of the path.
- **servers**: A set of server configurations used at path-level, overriding the top-level defined servers (if present).
- **parameters**: A set of parameters applicable to all operations in this path.

---

### Usage

- Define for a `routing` block:

```kotlin
routing {
    // Implement routes as usual
} apiPath {
    summary = "Some summary"
    description = "Some description"
    servers {
        add(urlString = "https://api.example.com") {
            description = "Some server description"
        }
    }
    pathParameter<Uuid>(name = "id") {
        description = "The unique identifier of the item."
    }
}
```

- Define for a `route` block:

```kotlin
routing {
    route("some-endpoint") {
        // Implement operations as usual (e.g., get, put, post, etc)
    } apiPath {
        summary = "Some summary"
        description = "Some description"
        servers {
            add(urlString = "https://api.example.com") {
                description = "Some server description"
            }
        }
        pathParameter<Uuid>(name = "id") {
            description = "The unique identifier of the item."
        }
    }
}
```

---

### [Internals - Debug Panel 🡲](../03-internals/01-debug-panel.md)

#### [🡰 Schema Annotation](../02-api-usage/09-schema-annotation.md)
