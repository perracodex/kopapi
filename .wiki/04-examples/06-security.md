## Example: Security

- Bearer security.

```kotlin
fun Route.findEmployeeByIdRoute() {
    get("/api/v1/employees/{employee_id}") {
        // Implement as usual
    } api {
        tags = setOf("Employee")
        summary = "Find an employee by ID."
        description = "Retrieve an employee's details by their unique ID"
        operationId = "findEmployeeById"
        pathParameter<Uuid>(name = "employee_id") {
            description = "The unique identifier of the employee."
        }
        response<Employee>(status = HttpStatusCode.OK) {
            description = "Employee found."
        }
        response<AppException.Response>(status = HttpStatusCode.NotFound) {
            description = "Employee not found."
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to employee details."
        }
    }
}
```

---

- OAuth2 security.

```kotlin
fun Route.createEmployeeRoute() {
    post("/api/v1/employees") {
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
        oauth2Security(name = "OAuth2") {
            description = "OAuth2 Authentication."

            authorizationCode {
                authorizationUrl = "https://example.com/auth"
                tokenUrl = "https://example.com/token"
                refreshUrl = "https://example.com/refresh"
                scope(name = "read:employees", description = "Read Data")
                scope(name = "write:employees", description = "Modify Data")
            }
        }
    }
}
```

---

- Skipping top-level security.

```kotlin
fun Route.publicRoute() {
    get("/api/v1/public") {
        // Implement as usual
    } api {
        tags = setOf("Public")
        summary = "Public endpoint."
        description = "A public endpoint that does not require authentication."
        operationId = "publicEndpoint"
        response<String>(status = HttpStatusCode.OK) {
            description = "Public endpoint response."
            contentType = setOf(ContentType.Text.Plain)
        }

        // Skip top-level security.
        noSecurity()
    }
}
```