### Top-Level Tags

The tags array at the top level of the OpenAPI specification serves as a top-level declaration
of all tags used within the API.

```kotlin
tags {
    add(name = "Items", description = "Operations related to items.")
}
```

- **Properties:**
    - `name`: The unique name of the tag.
    - `description`: Optional description of the tag.

---

### Example

```kotlin
install(Kopapi) {

    // ...
    // Top level tags.
    tags {
        add(name = "Items", description = "Operations related to items.")
        add(name = "Users", description = "Operations related to users.")
        add(name = "Orders", description = "Operations related to orders.")
    }
}
```

---

### [API Usage 🡲](../02-api-usage/01-route-api.md)

#### [🡰 Security](03-security.md)
