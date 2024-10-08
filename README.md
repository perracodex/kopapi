# [Kopapi](https://github.com/perracodex/kopapi)

Kopapi is library to generate OpenAPI documentation from [Ktor](https://ktor.io/) routes using `DSL`.

**Quick usage example before diving into the Wiki:**

```text
get("/items/{group_id}/{item_id?}") {
    // Handle GET request
} api { 
    summary = "Retrieve data items."
    description = "Fetches all items for a group."
    tags = Tags("Items", "Data")
    pathParameter<Uuid>("group_id") { description = "The Id of the group to resolve." }
    queryParameter<String>("item_id") { description = "Optional item Id to locate." }
    response<List<Item>>(HttpStatusCode.OK) { description = "Successful fetch" }
    response(HttpStatusCode.NotFound) { description = "Data not found" }
}
```

### Characteristics:

* Minimally invasive and flexible `DSL`.
* Possibility to define custom types.
* Conflict detection for duplicate Model definitions.
* Support for [Kotlinx](https://github.com/Kotlin/kotlinx.serialization) serialization annotations.
* Partial support for [Jackson](https://github.com/FasterXML/jackson-module-kotlin) serialization annotations.
* Output in `YAML` or `JSON` format.

---

### Wiki

* ### [Plugin Configuration](./.wiki/01.plugin-configuration.md)

* ### [Custom Types](./.wiki/02.custom-types.md)

* ### [API Usage](./.wiki/03.api-usage.md)
    - #### [Summary and Description](./.wiki/04.api-usage-summary-description.md)
    - #### [Tags](./.wiki/05.api-usage-tags.md)
    - #### [Parameters](./.wiki/06.api-usage-parameters.md)
    - #### [Request Bodies](./.wiki/07.api-usage-request-body.md)
    - #### [Responses](./.wiki/08.api-usage-responses.md)
    - #### [Security](./.wiki/09.api-usage-security.md)

* ### [Type Conflicts](./.wiki/10.type-conflicts.md)

* ### [Type Introspection](./.wiki/11.type-introspection.md)
