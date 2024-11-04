## Defining Responses

Define the possible responses to an API Operation using the `response` function.

```kotlin
// Response with a body type.
response<MyResponseType>(status = HttpStatusCode.OK) { }

// Response without a body type.
response(status = HttpStatusCode.OK) { }
```

### Multiple Content Types

Can add multiple content types to a response, if omitted, the default content type is `application/json`.

```kotlin
response<MyResponseType>(
  status = HttpStatusCode.OK,
  contentType = setOf(
    ContentType.Application.Json,
    ContentType.Application.Xml
  )
) {
    description = "Successfully retrieved the item."
}
```

---

### Additional Types

```kotlin
response<MyResponseType>(status = HttpStatusCode.OK) {
  // Register an additional type.
  addType<AnotherType>()

  // Register another type to the PDF ContentType
  // instead of the default.
  addType<YetAnotherType>(
    contentType = setOf(ContentType.Application.Pdf)
  )
}
```

This will result in the following OpenAPI definition:

```yaml
responses:
  "200":
    description: "MyResponseType response"
    content:
      application/json:
        schema:
          anyOf:
            - $ref: "#/components/schemas/MyResponseType"
            - $ref: "#/components/schemas/AnotherType"
      application/pdf:
        schema:
          - $ref: "#/components/schemas/YetAnotherType"
```

---

### Headers and Links to Responses

```kotlin
response<MyResponseType>(status = HttpStatusCode.OK) {
  header(name = "X-Rate-Limit") {
    description = "The number of allowed requests in the current period."
    required = true
  }
  link(operationId = "getNextItem") {
    description = "Link to the next item."
  }
}
```

- **Headers properties:**
  - `name`: The name of the header.
  - `description`: A human-readable description of the header.
  - `required`: Indicates whether the header is mandatory.
  - `deprecated`: Indicates whether the header is deprecated and should be avoided.


- **Link properties:**
  - `operationId`: The name of an existing, resolvable OAS operation.
  - `description`: A human-readable description of the link.

---

### Multiple Responses in an API Operation

It is also possible to define multiple responses for an API Operation.
In such case these will be combined by their status code.

Each response can define its own content types, headers, links, and additional types.
These will be merged and grouped according to their `status code` and `Content-Type`.

```kotlin
// Status code 200 OK with a body of MyResponseType.
response<MyResponseType>(status = HttpStatusCode.OK) {
  desription = "Example 1."
}

// Again status code 200 OK, but with a body of AnotherResponseType
// and more content-types.
response<AnotherResponseType>(
  status = HttpStatusCode.OK,
  contentType = setOf(
    ContentType.Application.Json,
    ContentType.Application.Xml
  )
) {
  description = "Example 2."
}

// Status code 404 Not Found.
response(status = HttpStatusCode.NotFound) {
  description = "Not Found."
}
```

- The above example will result in two grouped responses:
  - One for `200 OK` with a body of `MyResponseType` and `AnotherResponseType`.
  - Another for `404 Not Found`

```yaml
responses:
  "200":
    description: |-
      Example 1.
      Example 2.
    content:
      application/json:
        schema:
          anyOf:
            - $ref: "#/components/schemas/MyResponseType"
            - $ref: "#/components/schemas/AnotherResponseType"
      application/xml:
        schema:
          - $ref: "#/components/schemas/AnotherResponseType"
  "404":
    description: "Not found."
```

---

### Composition

The `composition` property allows to specify how multiple types associated with a ContentType should be combined.

- Only meaningful when multiple types are registered for the same ContentType.
- If no composition is defined, it defaults to `AnyOf`.
- If multiple responses with the same status code are defined, the last defined composition takes precedence.

```kotlin
response<MyResponseType>(status = HttpStatusCode.OK) {
  // Apply a global composition across all content types.
  composition = Composition.OneOf

  addType<AnotherType>()
}
```

--- 

### Full Example

```kotlin
api {
  response<MyResponseType>(
    status = HttpStatusCode.OK,
    contentType = setOf(
      ContentType.Application.Json,
      ContentType.Application.Xml
    )
  ) {
    // Optional description.
    description = "Successfully retrieved the item."

    // Optional Headers and Links.
    header(name = "X-Rate-Limit") {
      description = "The number of allowed requests in the current period."
      required = true
    }
    link(operationId = "getNextItem") {
      description = "Link to the next item."
    }

    // Optional composition.
    // Only meaningful when multiple types are registered.
    // If omitted, defaults to `AnyOf`.
    composition = Composition.AnyOf

    // Register an additional type.
    addType<AnotherType>()

    // Register another type to the PDF ContentType
    // instead of the default.
    addType<YetAnotherType>(
      contentType = setOf(ContentType.Application.Pdf)
    )
  }
}
```

### [Endpoints Security 🡲](07-security.md)

#### [🡰 Request Body](05-request-body.md)
