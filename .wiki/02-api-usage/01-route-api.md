## Defining OpenAPI Documentation

This guide provides an overview of how to use the api `DSL` to define OpenAPI-compatible metadata in Ktor routes.

---

### Basic Usage

Set the `api` DSL to a route that defines an HTTP method (e.g., `get`, `post`, `put`, `delete`):

```kotlin
routing {
    get("/items") {
        // Implement as usual
    } api {
        // Define API Operation metadata here.
    }
}
```

The `api` block defines the OpenAPI metadata for the route.

- It can be attached by `infix` notation: `}  api  {`
- Or by chained syntax: `}.api  {`

---

### Complete Example

Here's a complete example that combines the various elements:

```kotlin
get("/items/{id}") {
    // Implement as usual
} api {
    tags = setOf("Items", "Inventory")
    summary = "Retrieve an item by ID."
    description = "Fetches all items available in the inventory."
    description = "Supports pagination and filtering."
    operationId = "getItemById"

    // Path parameter
    pathParameter<Uuid>(name = "id") {
        description = "The unique identifier of the item."
    }

    // Query parameter
    queryParameter<Boolean>(name = "include_details") {
        description = "Whether to include detailed information."
        required = false
        defaultValue = false
    }

    // Header parameter
    headerParameter<String>(name = "X-Request-ID") {
        description = "An optional request identifier."
        required = false
    }

    // Request body (not applicable for GET, but shown here for completeness)
    // requestBody<MyRequestBodyType> {
    //     description = "Request body description."
    //     required = true
    // }

    // Responses
    response<ItemResponse>(status = HttpStatusCode.OK) {
        description = "Item retrieved successfully."
        contentType = setOf(ContentType.Application.Json, ContentType.Application.Xml)

        // Response headers
        header<Int>(name = "X-Rate-Limit") {
            description = "The number of allowed requests in the current period."
        }

        // Response links
        link(name = "SomeLinkName") {
            operationId = "someOperationId"
            description = "Some description."
            parameter(name = "some_id", value = "\$request.path.some_id")
        }
    }

    response(status = HttpStatusCode.NotFound) {
        description = "Item not found."
    }

    // Security
    bearerSecurity(name = "BearerAuth") {
        description = "HTTP bearer Authentication is required."
    }
    basicSecurity(name = "BasicAuth") {
        description = "HTTP basic Authentication is required."
    }
}
```

---

### [Summary and Description 🡲](02-summary-description.md)

#### [🡰 Tags](../01-plugin/04-tags.md)
