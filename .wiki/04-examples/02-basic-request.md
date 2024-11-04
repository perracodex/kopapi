## Example: Basic Request

- Path parameters

```kotlin
fun Route.findEmployeeByIdRoute() {
    get("v1/employees/{employee_id}") {
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
    }
}
```

- Query parameters

```kotlin
fun Route.schedulerDashboardRoute() {
    get("scheduler/dashboard/section_id/{group_id?}") {
        // Implement as usual
    } api {
        tags = setOf("Scheduler - Maintenance")
        summary = "Get the scheduler dashboard."
        description = "Get the scheduler dashboard, listing all scheduled tasks."
        operationId = "getSchedulerDashboard"
        pathParameter<Uuid>(name = "section_id") {
            description = "The section ID to filter tasks by."
        }
        queryParameter<Uuid>(name = "group_id") {
            description = "The group ID to filter tasks by."
            required = false
        }
        response<String>(status = HttpStatusCode.OK) {
            description = "The scheduler dashboard."
        }
    }
}
```
