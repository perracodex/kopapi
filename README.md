<a href="https://github.com/perracodex/kopapi">
    <img src=".wiki/images/logo.png" width="256" alt="Kopapi">
</a>

Kopapi is library to generate the OpenAPI documentation from [Ktor](https://ktor.io/) routes using `DSL`.

---

**Quick sample usage before diving into the Wiki:**

```kotlin
get("/items/{data_id}/{item_id?}") {
    // Handle GET request
} api {
  tags = setOf("Items", "Data")
    summary = "Retrieve data items."
    description = "Fetches all items for a group."
    operationId = "getDataItems"
    pathParameter<PathType.Uuid>("data_id") { description = "The data set Id." }
    queryParameter<String>("item_id") { description = "Optional item Id to locate." }
    response<List<Item>>(status = HttpStatusCode.OK) { description = "Successful fetch." }
    response(status = HttpStatusCode.NotFound) { description = "Data not found." }
    httpSecurity(name = "Authentication", method = AuthenticationMethod.BEARER) { description = "Access to data." }
}
```

### Characteristics:

* Lightweight and minimally invasive integration.
* Provides `Swagger UI` and `ReDoc` for testing and documentation.
* Flexible and expressive `DSL` builder.
* No unnatural modifications to your routes. Just add the `api` documentation block after it.
* Support for arbitrary types, including `generics`, `collections` and complex nested structures.
* Support for [Kotlinx](https://github.com/Kotlin/kotlinx.serialization) annotations.
* Support for [Jackson](https://github.com/FasterXML/jackson-module-kotlin) annotations (under development).
* Generate schema outputs in `YAML` or `JSON` format,

---

## Installation

Add the library to your project gradle dependencies. Make sure to replace `1.0.0` with the latest version.

```kotlin
dependencies {
  implementation("io.github.perracodex:kopapi:1.0.0")
}
```

### Version Compatibility

| **Kopapi** | **Ktor** |
|------------|----------|
| 1.0.0      | 3.0.0    |

---

### Wiki

* ### [Plugin Configuration](./.wiki/01.0.plugin-configuration.md)
  - #### [Servers](./.wiki/01.1.servers.md)
  - #### [Security](./.wiki/01.2.security.md)
  - #### [Top Level Tags](./.wiki/01.3.tags.md)
  - #### [Custom Types](./.wiki/01.4.custom-types)

* ### [API Usage](./.wiki/02.0.api-usage.md)
  - #### [Summary and Description](./.wiki/02.1.api-usage-summary-description.md)
  - #### [Path Operation Tags](./.wiki/02.2.api-usage-tags.md)
  - #### [Parameters](./.wiki/02.3.api-usage-parameters.md)
  - #### [Request Bodies](./.wiki/02.4.api-usage-request-body.md)
  - #### [Responses](./.wiki/02.5.api-usage-responses.md)
  - #### [Security](./.wiki/02.6.api-usage-security.md)

* ### Internals
  - #### [Type Conflicts](./.wiki/03.type-conflicts.md)
  - #### [Type Introspection](./.wiki/04.type-introspection.md)
  - #### [Debug Panel](./.wiki/05.debug-panel.md)

---

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

