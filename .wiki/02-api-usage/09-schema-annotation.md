## `@Schema` annotation

The `@Schema` annotation allows to provide additional field-level information for class properties,
aligned with OpenAPI 3.1 schema specification.

---

### Usage

```kotlin
@Schema(description = "Represents a person.")
data class Person(
  
  @Schema(description = "The first name of the person.", minLength = 3, maxLength = 50)
  val firstName: String,

  @Schema(description = "The age of the person.", minimum = "1", maximum = "120")
  val age: Int,

  @Schema(description = "The list of hobbies.", minItems = 1, maxItems = 5, uniqueItems = true)
  val hobbies: List<String>,

  @Schema(description = "Choice of colors.", defaultValue = "RED")
  val color: Color,

  @Schema(description = "The user's email address.", pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
  val email: String,

  @Schema(description = "The person's rating.", exclusiveMinimum = "0", exclusiveMaximum = "5", multipleOf = "0.5")
  val rating: Double,

  @Schema(description = "The total amount due, in whole dollars.", multipleOf = "5")
  val amountDue: Int

  // ...
)
```

---

### Properties

| Property         | Description                                                                    |
|------------------|--------------------------------------------------------------------------------|
| description      | A human-readable description of the field. (Can be used also with the class)   |
| defaultValue     | A default value for the field.                                                 |
| format           | Overrides the default format for the field allowing for custom formats.        |
| minLength        | Defines the minimum length for string values.                                  |
| maxLength        | Defines the maximum length for string values.                                  |
| pattern          | A regular expression pattern that a string field must match.                   |
| minimum          | Defines the inclusive lower bound for numeric types.                           |
| maximum          | Defines the inclusive upper bound for numeric types.                           |
| exclusiveMinimum | Defines a strict lower bound where the value must be greater than this number. |
| exclusiveMaximum | Defines a strict upper bound where the value must be less than this number.    |
| multipleOf       | Defines that the value must a multiple of the specified number.                |
| minItems         | Specifies the minimum number of items in an array.                             |
| maxItems         | Specifies the maximum number of items in an array.                             |
| uniqueItems      | Specifies that all items in an array must be unique.                           |

---

### Attribute Applicability

- Depending on the type of the field being annotated, only the relevant attributes are applicable:
    - **String Fields**: `minLength`, `maxLength`, `pattern`
    - **Numeric Fields**: `minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, `multipleOf`
    - **Array Fields**: `minItems`, `maxItems`, `uniqueItems`

- Non-relevant attributes to a type are ignored.
- The class itself can also be annotated, but only the `description` attribute is applicable.

---

### [Internals - Debug Panel 🡲](../03-internals/01-debug-panel.md)

#### [🡰 Servers](08-servers.md)
