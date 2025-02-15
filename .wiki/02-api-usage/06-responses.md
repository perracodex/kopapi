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
response<MyResponseType>(status = HttpStatusCode.OK) {
    description = "Successfully retrieved the item."
    contentType = setOf(
        ContentType.Application.Json,
        ContentType.Application.Xml
    )
}
```

---

### Additional Types

```kotlin
response<MyResponseType>(status = HttpStatusCode.OK) {
    // Register an additional type.
    addType<AnotherType>()

    // Register another type to XML only instead of the default JSON.
    addType<YetAnotherType>(
        contentType = setOf(ContentType.Application.Xml)
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
    // Headers to be included in the response.
    header<Int>(name = "X-Rate-Limit") {
        description = "The number of allowed requests in the current period."
    }
    header<String>(name = "X-Session-Token") {
        description = "The session token for the user."
        schema {
            pattern = "^[A-Za-z0-9_-]{20,50}$"
            minLength = 20
            maxLength = 50
        }
    }

    // Links to other operations.
    link(name = "GetEmployeeDetails") {
        operationId = "getEmployeeDetails"
        description = "Retrieve information about this employee."
        parameter(name = "employee_id", value = "\$request.path.employee_id")
    }
    link(name = "UpdateEmployeeStatus") {
        operationId = "updateEmployeeStatus"
        description = "Link to update the status of this employee."
        parameter(name = "employee_id", value = "\$request.path.employee_id")
        parameter(name = "status", value = "active")
        requestBody = "{\"status\": \"active\"}"
    }
    link(name = "ListEmployeeBenefits") {
        operationRef = "/api/v1/benefits/list"
        description = "List all benefits available to the employee."
        parameter(name = "employee_id", value = "\$request.path.employee_id")
    }
}
```

- For better organization, the `headers` and `links` blocks can be used to group multiple headers and links together.
- Note that this is just syntactic sugar and has the same effect as defining them individually.

```kotlin
response<MyResponseType>(status = HttpStatusCode.OK) {
    headers {
        add<Int>(name = "X-Rate-Limit") {
            description = "The number of allowed requests in the current period."
        }
        add<String>(name = "X-Session-Token") {
            description = "The session token for the user."
            schema {
                pattern = "^[A-Za-z0-9_-]{20,50}$"
                minLength = 20
                maxLength = 50
            }
        }
    }

    links {
        add(name = "GetEmployeeDetails") {
            operationId = "getEmployeeDetails"
            description = "Retrieve information about this employee."
            parameter(name = "employee_id", value = "\$request.path.employee_id")
        }
        add(name = "UpdateEmployeeStatus") {
            operationId = "updateEmployeeStatus"
            description = "Link to update the status of this employee."
            parameter(name = "employee_id", value = "\$request.path.employee_id")
            parameter(name = "status", value = "active")
            requestBody = "{\"status\": \"active\"}"
        }
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
```
```kotlin
// Again status code 200 OK, but with a body of AnotherResponseType and more content-types.
response<AnotherResponseType>(status = HttpStatusCode.OK) {
    description = "Example 2."
    contentType = setOf(
        ContentType.Application.Json,
        ContentType.Application.Xml
    )
}
```
```kotlin
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
    // Apply the common composition across all content types.
    composition = Composition.OneOf

    addType<AnotherType>()
}
```

---

### Defining Additional Schema Properties

When the response or header type defines a primitive type such as strings, numbers, including arrays,
additional properties can be set using the `schema` block.

```kotlin
response<String>(status = HttpStatusCode.OK) {
    description = "The unique identifier for the newly created resource."
    schema {
        minLength = 10
    }

    headers {
        add<String>(name = "X-Session-Token") {
            description = "The session token for the user, required for authenticated requests."
            schema {
                pattern = "^[A-Za-z0-9_-]{36}$"
                minLength = 36
                maxLength = 36
            }
        }

        add<String>(name = "X-RateLimit-Remaining") {
            description = "The number of requests remaining in the current rate limit window."
            schema {
                minimum = 0
            }
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
| contentEncoding  | May be used to specify the Content-Encoding for the schema.                    |
| contentMediaType | May be used to specify the Media-Type for the schema.                          |                                                                             |
| minimum          | Defines the inclusive lower bound for numeric types.                           |
| maximum          | Defines the inclusive upper bound for numeric types.                           |
| exclusiveMinimum | Defines a strict lower bound where the value must be greater than this number. |
| exclusiveMaximum | Defines a strict upper bound where the value must be less than this number.    |
| multipleOf       | Specifies that the type’s value must be a multiple of this number.             |
| minItems         | Specifies the minimum number of items in an array type.                        |
| maxItems         | Specifies the maximum number of items in an array type.                        |
| uniqueItems      | Specifies that all items in an array type must be unique.                      |

- Depending on the type, only the relevant attributes are applicable:
  - **String Types**: `minLength`, `maxLength`, `pattern`, `contentEncoding`, `contentMediaType`
  - **Numeric Types**: `minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, `multipleOf`
  - **Array Types**: `minItems`, `maxItems`, `uniqueItems`

- Non-relevant attributes to a type are ignored.

- For complex object types the `schema` block is ignored. Use instead the `@Schema` annotation directly on the class type.

--- 

### Full Example

```kotlin
api {
    response<MyResponseType>(status = HttpStatusCode.OK) {
        // Optional description.
        description = "Successfully retrieved the item."

        // Override the default content type.
        contentType = setOf(
            ContentType.Application.Json,
            ContentType.Application.Xml
        )

        // Optional Headers and Links.
        header<Int>(name = "X-Rate-Limit") {
            description = "The number of allowed requests in the current period."
            schema {
                minimum = 0
            }
        }
        link(name = "GetEmployeeDetails") {
            operationId = "getEmployeeDetails"
            description = "Retrieve information about this employee."
            parameter(name = "employee_id", value = "\$request.path.employee_id")
        }

        // Optional composition.
        // Only meaningful when multiple types are registered.
        // If omitted, defaults to `AnyOf`.
        composition = Composition.AnyOf

        // Register an additional type.
        addType<AnotherType>()

        // Register another type but to XML only.
        addType<YetAnotherType> {
            contentType = setOf(ContentType.Application.Xml)
        }
    }
}
```

### [Endpoints Security 🡲](07-security.md)

#### [🡰 Request Body](05-request-body.md)
