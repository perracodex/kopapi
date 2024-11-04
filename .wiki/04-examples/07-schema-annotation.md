## Example: Schema Annotation

```kotlin
@Serializable
@Schema(description = "Represents a concrete employee.")
public data class Employee(
    @Schema(description = "The employee's id.")
    val id: Uuid,

    @Schema(description = "The first name of the employee.", minLength = 3, maxLength = 50)
    val firstName: String,

    @Schema(description = "The last name of the employee.", minLength = 3, maxLength = 50)
    val lastName: String,

    @Schema(description = "The full name of the employee, computed as 'lastName, firstName'.")
    val fullName: String,

    @Schema(
        description = "The unique work email of the employee.",
        format = "email",
        maxLength = 255,
        pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )
    val workEmail: String,

    @Schema(description = "The date of birth of the employee.")
    val dob: LocalDate,

    @Schema(description = "The age of the employee, computed from [dob].", minimum = "1", maximum = "120")
    val age: Int,

    @Schema(description = "The [MaritalStatus] of the employee.", defaultValue = "SINGLE")
    val maritalStatus: MaritalStatus,

    @Schema(description = "The [Honorific] or title of the employee.", defaultValue = "UNKNOWN")
    val honorific: Honorific,

    @Schema(description = "Optional [Contact] details of the employee.")
    val contact: Contact?,

    @Schema(description = "The metadata of the record.")
    val meta: Meta
)
```