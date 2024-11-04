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

---

### [Request Body 🡲](05-request-body.md)

#### [🡰 Path Operation Tags](03-tags.md)
