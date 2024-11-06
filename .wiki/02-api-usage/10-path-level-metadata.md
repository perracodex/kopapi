## Path-Level Metadata

Metadata can be set at path-level, which will be shared by all operations in that path.
Currently, can be set the following metadata:

- **summary**: A brief summary of the path.
- **description**: A human-readable description of the path.
- **servers**: An array of server objects to be used by the path. Syntax is the same as the root level `servers` object.

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
    }
}
```

---

### [Internals - Debug Panel 🡲](../03-internals/01-debug-panel.md)

#### [🡰 Schema Annotation](../02-api-usage/09-schema-annotation.md)
