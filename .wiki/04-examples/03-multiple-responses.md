## Example: Multiple Responses

```kotlin  
fun Route.createTokenRoute() {
    post("/auth/token/create") {
        // Implement as usual
    } api {
        tags = setOf("Token")
        summary = "Create a new JWT token."
        description = "Generates a new JWT token using Basic Authentication."
        operationId = "createToken"
        response<String>(status = HttpStatusCode.OK) {
            description = "The generated JWT token."
            contentType = seyOfType(ContentType.Text.Plain)
        }
        response(status = HttpStatusCode.Unauthorized) {
            description = "No valid credentials provided."
        }
        response(status = HttpStatusCode.InternalServerError) {
            description = "Failed to generate token."
        }
        basicSecurity(name = "TokenCreation") {
            description = "Generates a new JWT token using Basic Authentication."
        }
    }
}
```
