## Defining Parameters

### Path Parameters

Define parameters that are part of the URL path:

```kotlin
api {
    pathParameter<Uuid>(name = "id") {
        description = "The unique identifier of the item."
    }
}
```

- **Properties:**
  - `description`: A description of the parameter's purpose and usage.
  - `required`: Indicates whether the parameter is mandatory for the API call.
  - `defaultValue`: The default value for the parameter if one is not provided.
  - `style`: The style in which the parameter is serialized in the URL.
  - `deprecated`: Indicates if the parameter is deprecated and should be avoided.
  - `schema`: Additional properties for the type.

---

### Query Parameters

Define query parameters:

```kotlin
api {
    queryParameter<Int>(name = "page") {
        description = "The page number to retrieve."
        required = false
        defaultValue = DefaultValue.ofInt(value = 1)
    }
    queryParameter<Int>(name = "size") {
        description = "The number of items per page."
        required = false
        defaultValue = DefaultValue.ofInt(value = 10)
    }
}
```

- **Properties:**
  - `description`: A description of the parameter's purpose and usage.
  - `required`: Indicates whether the parameter is mandatory for the API call.
  - `defaultValue`: The default value for the parameter if one is not provided.
  - `style`: The style in which the parameter is serialized in the URL.
  - `explode`: Whether to send arrays and objects as separate parameters.
  - `deprecated`: Indicates if the parameter is deprecated and should be avoided.
  - `schema`: Additional properties for the type.

---

### Header Parameters

Define header parameters:

```kotlin
api {
    headerParameter<String>(name = "X-Custom-Header") {
        description = "A custom header for special purposes."
        required = true
    }
}
```

- **Properties:**
  - `description`: A description of the parameter's purpose and usage.
  - `required`: Indicates whether the parameter is mandatory for the API call.
  - `defaultValue`: The default value for the parameter if one is not provided.
  - `style`: The style in which the parameter is serialized in the URL.
  - `deprecated`: Indicates if the parameter is deprecated and should be avoided.
  - `schema`: Additional properties for the type.

---

### Cookie Parameters

Define cookie parameters:

```kotlin
api {
    cookieParameter<String>(name = "session") {
        description = "The session ID for authentication."
    }
}
```

- **Properties:**
  - `description`: A description of the parameter's purpose and usage.
  - `required`: Indicates whether the parameter is mandatory for the API call.
  - `defaultValue`: The default value for the parameter if one is not provided.
  - `style`: The style in which the parameter is serialized in the URL.
  - `explode`: Whether to send arrays and objects as separate parameters.
  - `deprecated`: Indicates if the parameter is deprecated and should be avoided.
  - `schema`: Additional properties for the type.

---

### Parameters Block

- For better organization, the `parameters` block can be used to group multiple parameters together:
- Note that this is just syntactic sugar and has the same effect as defining them individually.

```kotlin
api {
    parameters {
        pathParameter<Uuid>(name = "id") {
            description = "The unique identifier of the item."
        }
        queryParameter<Int>(name = "page") {
            description = "The page number to retrieve."
            required = false
            defaultValue = DefaultValue.ofInt(value = 1)
        }
        queryParameter<Int>(name = "size") {
            description = "The number of items per page."
            required = false
            defaultValue = DefaultValue.ofInt(value = 10)
        }
    }
}
```

---

### Defining Additional Schema Properties

When the parameter defines a primitive type such as strings, numbers, including arrays,
additional properties can be set using the `schema` block.

```kotlin
api {
    queryParameter<Int>(name = "page") {
        description = "The page number to retrieve."
        required = false
        defaultValue = DefaultValue.ofInt(value = 1)
      
        // Define additional properties for the above defined Int type
        schema {
            minimum = 1
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

### [Request Body 🡲](05-request-body.md)

#### [🡰 Path Operation Tags](03-tags.md)
