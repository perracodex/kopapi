## Defining the Request Body

Specify the structure and type of the request body.

- Only one request body can be defined per route.

```kotlin
requestBody<MyRequestBodyType> {
    description = "The data required to create a new item."
}
```

- **Properties:**
    - `description`: A description of the request body's content and what it represents.
  - `required`: Indicates whether the request body is mandatory for the API call. (Default: `true`)
  - `composition`: The composition strategy when multiple types are registered. (Default: `Composition.ANY_OF`

---

### Additional Types and Content-Types

Besides the primary request body type, you can also register additional types, including multiple `ContentTypes`.

```kotlin
requestBody<MyRequestBodyType> {
  // Override the default content type.
  contentType = setOf(
    ContentType.Application.Json,
    ContentType.Application.Xml
  )

  // Register an additional type.
  addType<AdditionalType>()

  // Register another type but to XML only.
  addType<YetAnotherTpe> {
    contentType = setOf(ContentType.Application.Xml)
  }
}
```

This will result in the following OpenAPI definition:

```yaml
requestBody:
  required: true
  content:
    application/json:
      schema:
        anyOf:
          - $ref: "#/components/schemas/MyRequestBodyType"
          - $ref: "#/components/schemas/AdditionalType"
    application/xml:
      schema:
        anyOf:
          - $ref: "#/components/schemas/MyRequestBodyType"
          - $ref: "#/components/schemas/AdditionalType"
          - $ref: "#/components/schemas/YetAnotherTpe"
```

---

### Multipart Form Data

To define a request body as `multipart` use the `multipart` function.

- **`contentType`**: Specify the `ContentType.MultiPart`. Default: `ContentType.MultiPart.FormData`.
- Use Ktor's `PartData` subclasses to specify the part type.

```kotlin
multipart {
  part<PartData.FileItem>("myFilePart") {
    description = "The file to upload."
  }
}
```

Or specify the content details explicitly:

```kotlin
multipart {
  contentType = ContentType.MultiPart.Encrypted

  part<PartData.FormItem>("myFormPart") {
    description = "The form data."
  }
}
```

- **Properties of Part:**
  - `description`: A description of the part's content and what it represents.
  - `required`: Indicates whether the part is mandatory for the API call. (Default: `true`)
  - `schemaType`: Specify the type for the schema, such as STRING, NUMBER, etc. (Optional).
  - `schemaFormat`: Specify the format for the schema, such as BINARY, UUID, etc. (Optional).
  - `contentType`: Optional parameter to explicitly define the content type of the part. By default, inferred on the type of `PartData`.

---

### Example

- Note that instead of `Unit` it is also possible to define an explicit type for the `requestBody` to produce a combined request.

```kotlin
requestBody<Unit> {
  // Upload product details, including product images and metadata.
  multipart {
    contentType = ContentType.MultiPart.FormData

    // Upload the primary product image (JPEG)
    part<PartData.FileItem>("primaryImage") {
      description = "The primary image of the product."
      contentType = ContentType.Image.JPEG
      schemaType = ApiType.STRING
      schemaFormat = ApiFormat.BINARY
    }

    // Upload the secondary product image (optional)
    part<PartData.FileItem>("secondaryImage") {
      description = "An optional secondary image of the product."
      contentType = ContentType.Image.JPEG
      schemaType = ApiType.STRING
      schemaFormat = ApiFormat.BINARY
      required = false
    }

    // Add a form field for the product name (plain text)
    part<PartData.FormItem>("productName") {
      description = "The name of the product."
      schemaType = ApiType.STRING
    }

    // Add a form field for the product price
    part<PartData.FormItem>("price") {
      description = "The price of the product in USD."
      schemaType = ApiType.NUMBER
      schemaFormat = ApiFormat.FLOAT
    }

    // Upload a JSON object for product metadata (optional)
    part<PartData.FormItem>("metadata") {
      description = "Additional metadata about the product."
      contentType = ContentType.Application.Json
      schemaType = ApiType.OBJECT
      required = false
    }
  }
}
```

Resulting Output:

```yaml
requestBody:
  required: true
  content:
    multipart/form-data:
      schema:
        type: object
        properties:
          primaryImage:
            description: "The primary image of the product."
            content:
              image/jpeg:
                schema:
                  type: "string"
                  format: "binary"
          secondaryImage:
            description: "An optional secondary image of the product."
            content:
              image/jpeg:
                schema:
                  type: "string"
                  format: "binary"
          productName:
            description: "The name of the product."
            content:
              text/plain:
                schema:
                  type: "string"
          price:
            description: "The price of the product in USD."
            content:
              text/plain:
                schema:
                  type: "number"
                  format: "float"
          metadata:
            description: "Additional metadata about the product."
            content:
              application/json:
                schema:
                  type: "object"
        required:
          - primaryImage
          - productName
          - price
```

---

### [Responses 🡲](06-responses.md)

#### [🡰 Parameters](04-parameters.md)
