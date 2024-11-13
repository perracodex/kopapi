## Conflict Detection

When multiple types share the same name but have different structures,
the plugin generates separate object schemas for each type within the OpenAPI specification.

This approach can create ambiguities, as OpenAPI references schemas by their names,
leading to potential conflicts when identical names correspond to different structures.

---

### How `Kopapi` Handles Conflicts:

- **Identical Schema Names:** Kopapi allows multiple schemas to have the same name, regardless of their differing structures.
  It does not automatically rename or differentiate these schemas.

- **Informative Descriptions:** To mitigate the ambiguity caused by identical schema names,
  the generated OpenAPI main description includes the details about each conflicting types,
  so they can be identified and be resolved manually.

To resolve conflicts, it is recommended to rename the classes to be unique, or alternatively,
use annotations such as `@SerialName` in Kotlinx Serialization, or `@JsonTypeName` in Jackson,

---

### Example Scenario:

Suppose you have two classes named `User` in different packages:

```kotlin
data class User(
  val id: Uuid,
  val adminLevel: Int
)
```

```kotlin
data class User(
  val id: Uuid,
  val loyaltyPoints: Int,
  val discount: DiscountRate
)
```

Both `User` classes will generate schemas with the same name in the OpenAPI specification:

```yaml
"User": {
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "format": "uuid"
    },
    "adminLevel": {
      "type": "integer"
    }
  },
  "description": "com.example.admin.User"
}

"User": {
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "format": "uuid"
    },
    "loyaltyPoints": {
      "type": "integer"
    },
    "discount": {
      "$ref": "#/components/schemas/DiscountRate"
    }
  },
  "description": "com.example.customer.User"
}
```

This results in a conflict, as the OpenAPI specification contains two schemas with the same name `User`.

---

### [Internals - Type Introspection 🡲](03-type-introspection.md)

#### [🡰 Internals - Debug Panel](01-debug-panel.md)
