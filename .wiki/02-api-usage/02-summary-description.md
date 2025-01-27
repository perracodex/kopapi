## Summary and Description

Provide a brief summary and detailed description of the endpoint:

```kotlin
api {
  operationId = "getItems"
    summary = "Retrieve a list of items."
    description = "Fetches all items available in the inventory."
    description = "Supports pagination and filtering."
}
```

- `operationId` is a unique identifier for the endpoint. Must be unique across all endpoints.
- Multiple `description` calls can be used to provide additional information.
  These will be appended with a newline character between each description.
- The `summary` can also be declared multiple times, but will be appended with a space character between each summary.

--- 

### [Path Operation Tags 🡲](03-tags.md)

#### [🡰 API Usage](01-route-api.md)