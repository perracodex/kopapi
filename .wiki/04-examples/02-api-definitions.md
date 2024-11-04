## Example: Defining API Documentation

---

```kotlin
fun Route.findEmployeeByIdRoute() {
    get("v1/employees/{employee_id}") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val employee: Employee = service.findById(employeeId = employeeId)
            ?: throw EmployeeError.EmployeeNotFound(employeeId = employeeId)
        call.respond(status = HttpStatusCode.OK, message = employee)
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
            description = "Access to employee data."
        }
    }
}
```

---

```kotlin
fun Route.createEmployeeRoute() {
    post("v1/employees") {
        val request: EmployeeRequest = call.receive<EmployeeRequest>()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(call.getContext()) }
        val employee: Employee = service.create(request = request).getOrThrow()
        call.respond(status = HttpStatusCode.Created, message = employee)
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
        bearerSecurity(name = "Authentication") {
            description = "Access to employee data."
        }
    }
}
```

---

```kotlin  
fun Route.createTokenRoute() {
    rateLimit(configuration = RateLimitName(name = "MyRateLimit")) {
        authenticate("MyAuth") {
            post("auth/token/create") {
                call.respondWithToken()
            } api {
                tags = setOf("Token")
                summary = "Create a new JWT token."
                description = "Generates a new JWT token using Basic Authentication."
                operationId = "createToken"
                basicSecurity(name = "TokenCreation") {
                    description = "Generates a new JWT token using Basic Authentication."
                }
                response<String>(status = HttpStatusCode.OK, contentType = ContentType.Text.Plain) {
                    description = "The generated JWT token."
                }
                response(status = HttpStatusCode.Unauthorized) {
                    description = "No valid credentials provided."
                }
                response(status = HttpStatusCode.InternalServerError) {
                    description = "Failed to generate token."
                }
            }
        }
    }
}
```