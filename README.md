<a href="https://github.com/perracodex/kopapi">
    <img src=".wiki/images/logo.png" width="256" alt="Kopapi">
</a>

**Kopapi** is library to generate OpenAPI documentation from [Ktor](https://ktor.io/) routes.

---

**Quick sample usage before diving into the Wiki:**

```kotlin
get("/items/{data_id}") {
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
    defaultResponse { description = "Unexpected issue." }
    bearerSecurity(name = "Authentication") { description = "Access to data." }
}
```

### Characteristics:

* Support for arbitrary types, including `generics`, `collections` and complex nested structures.
* Schema outputs in `YAML` or `JSON` format.
* Provides `Swagger UI` and `ReDoc` out of the box.
* Flexible and expressive `DSL` builder.
* Support for [Kotlinx](https://github.com/Kotlin/kotlinx.serialization) annotations, ([Jackson](https://github.com/FasterXML/jackson-module-kotlin)
   are partially supported).
* Minimally invasive integration. No unnatural modifications to the routes.
* Well documented.

---

## Installation

Add the library to your project gradle dependencies.

```kotlin
dependencies {
    implementation("io.github.perracodex:kopapi:<VERSION>>")
}
```

### Version Compatibility

| **Kopapi** | **Ktor**  | **Kotlin** |
|------------|-----------|------------|
| 1.0.15     | \>= 3.3.0 | \>= 2.2.20 |
| 1.0.14     | \>= 3.2.3 | \>= 2.2.0  |
| 1.0.13     | \>= 3.1.2 | \>= 2.1.20 |
| 1.0.12     | = 3.1.1   | = 2.1.10   |

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

To see the library in action, check the [Krud](https://github.com/perracodex/krud) repository:

- https://github.com/perracodex/krud/blob/main/krud-core/base/src/main/kotlin/krud/base/plugins/ApiSchema.kt
- https://github.com/perracodex/krud/tree/main/krud-domain/employee/src/main/kotlin/krud/domain/employee/api
- https://github.com/perracodex/krud/tree/main/krud-core/access/src/main/kotlin/krud/access/domain/token/api

---

### License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.
