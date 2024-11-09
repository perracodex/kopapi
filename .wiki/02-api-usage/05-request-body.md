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

### Defining Additional Schema Properties

When the request body defines a primitive type such as strings, numbers, including arrays,
additional properties can be set using the `schema` block.

```kotlin
requestBody<String> {
  schema {
    minLength = 10
    maxLength = 50
  }

  addType<Int> {
    schema {
      minimum = 0
      maximum = 100
    }
  }
}
```

| Property         | Description                                                                    |
|------------------|--------------------------------------------------------------------------------|
| format           | Overrides the default format for the type allowing for custom formats.         |
| minLength        | Defines the minimum length for string types.                                   |
| maxLength        | Defines the maximum length for string types.                                   |
| pattern          | A regular expression pattern that a string type must match.                    |
| minimum          | Defines the inclusive lower bound for numeric types.                           |
| maximum          | Defines the inclusive upper bound for numeric types.                           |
| exclusiveMinimum | Defines a strict lower bound where the value must be greater than this number. |
| exclusiveMaximum | Defines a strict upper bound where the value must be less than this number.    |
| multipleOf       | Specifies that the type’s value must be a multiple of this number.             |
| minItems         | Specifies the minimum number of items in an array type.                        |
| maxItems         | Specifies the maximum number of items in an array type.                        |
| uniqueItems      | Specifies that all items in an array type must be unique.                      |

- Depending on the type, only the relevant attributes are applicable:
  - **String Types**: `minLength`, `maxLength`, `pattern`
  - **Numeric Types**: `minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, `multipleOf`
  - **Array Types**: `minItems`, `maxItems`, `uniqueItems`

- Non-relevant attributes to a type are ignored.

- For complex object types the `schema` block is ignored. Use instead the `@Schema` annotation directly on the class type.

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
```kotlin
multipart {
  contentType = ContentType.MultiPart.Signed

    part<PartData.FormItem>("myFormPart") {
        description = "The form data."
    }
}
```

- **Properties of `part`:**
  - `description`: A description of the part's content and what it represents.
  - `required`: Indicates whether the part is mandatory for the API call. (Default: `true`)
  - `contentType`: Optional set of `ContentType` items for the part.
  - `schemaType`: Specify the type for the schema, such as STRING, NUMBER, etc. (Optional).
  - `schemaFormat`: Specify the format for the schema, such as BINARY, UUID, etc. (Optional).

```kotlin
multipart {
  part<PartData.FileItem>("myFilePart") {
    description = "The file to upload."

    contentType = setOf(
      ContentType.Image.JPEG,
      ContentType.Image.PNG
    )

    header<Int>(name = "SomeHader") {
      description = "Some header value."
      schema {
        minimum = 1
        maximum = 10
      }
    }
  }
}
```

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
          contentType = setOf(ContentType.Image.JPEG, ContentType.Image.PNG)
            schemaType = ApiType.STRING
            schemaFormat = ApiFormat.BINARY
        }

        // Upload the secondary product image (optional)
        part<PartData.FileItem>("secondaryImage") {
            description = "An optional secondary image of the product."
          contentType = setOf(ContentType.Image.JPEG, ContentType.Image.PNG)
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
          contentType = setOf(ContentType.Application.Json)
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
