<a href="https://github.com/perracodex/kopapi">
    <img src=".wiki/images/logo.png" width="256" alt="Kopapi">
</a>

Kopapi is library to generate the OpenAPI documentation from [Ktor](https://ktor.io/) routes using `DSL`.

---

**Quick sample usage before diving into the Wiki:**

```kotlin
get("/items/{group_id}/{item_id?}") {
    // Handle GET request
} api {
    summary = "Retrieve data items."
    description = "Fetches all items for a group."
    tags("Items", "Data")
    pathParameter<Uuid>("group_id") { description = "The Id of the group to resolve." }
    queryParameter<String>("item_id") { description = "Optional item Id to locate." }
    response<List<Item>>(HttpStatusCode.OK) { description = "Successful fetch" }
    response(HttpStatusCode.NotFound) { description = "Data not found" }
}
```

### Characteristics:

* Minimally invasive.
* Comprehensive and flexible `DSL`.
* Conflict detection for duplicate Model definitions.
* Support for [Kotlinx](https://github.com/Kotlin/kotlinx.serialization) serialization annotations.
* Partial support for [Jackson](https://github.com/FasterXML/jackson-module-kotlin) serialization annotations.
* Output in `YAML` or `JSON` format.
* Debug panel to identify issues with the Schema generation.

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

