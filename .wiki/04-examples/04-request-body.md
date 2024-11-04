## Example: Request Body

```kotlin
fun Route.createEmployeeRoute() {
    post("v1/employees") {
        // Implement as usual
    } api {
        tags = setOf("Employee")
        summary = "Create an employee."
        description = "Create a new employee in the system."
        operationId = "createEmployee"
        requestBody<EmployeeRequest> {
            description = "The employee to create."
        }
        response<Employee>(status = HttpStatusCode.Created) {
            description = "Employee created."
        }
    }
}
```

---

- Request Body with Multiple Content Types and additional types

```kotlin  
fun Route.createEmployeeRoute() {
    post("v1/employees") {
        // Implement as usual
    } api {
        tags = setOf("Employee")
        summary = "Create an employee."
        description = "Create a new employee in the system."
        operationId = "createEmployee"
        requestBody<EmployeeRequest>(contentType = setOf(ContentType.Application.Xml, ContentType.Application.Json)) {
            description = "The employee to create."
            required = true
            composition = Composition.AnyOf
            addType<ExtendedEmployeeRequest>()
        }
        response<Employee>(status = HttpStatusCode.Created) {
            description = "Employee created."
        }
    }
}
```
