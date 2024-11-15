<a href="https://github.com/perracodex/kopapi">
    <img src=".wiki/images/logo.png" width="256" alt="Kopapi">
</a>

**Kopapi** is library to generate OpenAPI documentation from [Ktor](https://ktor.io/) routes.

---

**Quick sample usage before diving into the Wiki:**

```kotlin
get("/items/{data_id}/{item_id?}") {
    // Implement as usual
} api {
    tags = setOf("Items", "Data")
    summary = "Retrieve data items."
    description = "Fetches all items for a group."
    operationId = "getDataItems"
    pathParameter<Uuid>("data_id") { description = "The data set Id." }
    queryParameter<String>("item_id") { description = "Optional item Id to locate." }
    response<List<Item>>(status = HttpStatusCode.OK) { description = "Successful fetch." }
    response(status = HttpStatusCode.NotFound) { description = "Data not found." }
    bearerSecurity(name = "Authentication") { description = "Access to data." }
}
```

### Characteristics:

* Lightweight and minimally invasive integration.
* Provides `Swagger UI` and `ReDoc` for testing and documentation.
* Flexible and expressive `DSL` builder.
* No unnatural modifications to your routes. Just add the `api` documentation block after it.
* Support for arbitrary types, including `generics`, `collections` and complex nested structures.
* Support for [Kotlinx](https://github.com/Kotlin/kotlinx.serialization) and [Jackson](https://github.com/FasterXML/jackson-module-kotlin)
  annotations (under development).
* Generate schema outputs in `YAML` or `JSON` format.
* Well documented.

---

## Installation

Add the library to your project gradle dependencies. Make sure to replace `1.0.2` with the latest version.

```kotlin
dependencies {
    implementation("io.github.perracodex:kopapi:1.0.2")
}
```

### Version Compatibility

| **Kopapi** | **Ktor**  |
|------------|-----------|
| \>= 1.0.2  | \>= 3.0.0 |

---

### Wiki

* ### Plugin
    - #### [Plugin Configuration](https://github.com/perracodex/kopapi/wiki/01.-Plugin:-Configuration)
    - #### [Servers](https://github.com/perracodex/kopapi/wiki/02.-Plugin:-Servers)
    - #### [Security](https://github.com/perracodex/kopapi/wiki/03.-Plugin:-Security)
    - #### [Top Level Tags](https://github.com/perracodex/kopapi/wiki/04.-Plugin:-Tags)

* ### API
    - #### [API Usage](https://github.com/perracodex/kopapi/wiki/05.-API-Usage:-Routes)
    - #### [Summary and Description](https://github.com/perracodex/kopapi/wiki/06.-API-Usage:-Summary-and-Description)
    - #### [Path Operation Tags](https://github.com/perracodex/kopapi/wiki/07.-API-Usage:-Path-Operation-Tags)
    - #### [Parameters](https://github.com/perracodex/kopapi/wiki/08.-API-Usage:-Parameters)
    - #### [Request Bodies](https://github.com/perracodex/kopapi/wiki/09.-API-Usage:-Request-Body)
    - #### [Responses](https://github.com/perracodex/kopapi/wiki/10.-API-Usage:-Responses)
    - #### [Security](https://github.com/perracodex/kopapi/wiki/11.-API-Usage:-Security)
    - #### [Servers](https://github.com/perracodex/kopapi/wiki/12.-API-Usage:-Servers)
    - #### [Schema Annotation](https://github.com/perracodex/kopapi/wiki/13.-API-Usage:-@Schema-Annotation)
    - #### [Path-Level Metadata](https://github.com/perracodex/kopapi/wiki/14.-API-Usage:-Path%E2%80%90Level-Metadata)

* ### Internals
    - #### [Debug Panel](https://github.com/perracodex/kopapi/wiki/15.-Internals:-Debug-Panel)
    - #### [Type Conflicts](https://github.com/perracodex/kopapi/wiki/16.-Internals:-Conflict-Detection)
    - #### [Type Introspection](https://github.com/perracodex/kopapi/wiki/17.-Internals:-Type-Introspection)

* ### Examples
    - #### [Plugin Setup](https://github.com/perracodex/kopapi/wiki/18.-Examples:-Plugin-Setup)
    - #### [Basic Request](https://github.com/perracodex/kopapi/wiki/19.--Examples:-Basic-Request)
    - #### [Multiple Responses](https://github.com/perracodex/kopapi/wiki/20.-Examples:-Multiple-Responses)
    - #### [RequestBody](https://github.com/perracodex/kopapi/wiki/21.-Examples:-Request-Body)
    - #### [Multipart](https://github.com/perracodex/kopapi/wiki/22.-Examples:-Multi%E2%80%90Part)
    - #### [Security](https://github.com/perracodex/kopapi/wiki/23.-Examples:-Security)
    - #### [Schema Annotation](https://github.com/perracodex/kopapi/wiki/24.-Examples:-Schema-Annotation)

---

To see the library in action, check the [Kcrud](https://github.com/perracodex/kcrud) repository:

- https://github.com/perracodex/kcrud/blob/main/kcrud-core/src/main/kotlin/kcrud/core/plugins/ApiSchema.kt
- https://github.com/perracodex/kcrud/tree/main/kcrud-employee/src/main/kotlin/kcrud/domain/employee/api
- https://github.com/perracodex/kcrud/tree/main/kcrud-access/src/main/kotlin/kcrud/access/token/api/operate
- https://github.com/perracodex/kcrud/tree/main/kcrud-access/src/main/kotlin/kcrud/access/rbac/api
- https://github.com/perracodex/kcrud/tree/main/kcrud-core/src/main/kotlin/kcrud/core/scheduler/api

---

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

