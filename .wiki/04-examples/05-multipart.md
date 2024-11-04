## Example: Multi-Part

```kotlin   
fun Route.uploadDocumentsRoute() {
    post("v1/document/{owner_id?}") {
        // Implement as usual
    } api {
        tags = setOf("Document")
        summary = "Upload a document."
        description = "Uploads a document into the system storage."
        operationId = "uploadDocument"
        queryParameter<Uuid>(name = "owner_id") {
            description = "The owner of the document."
        }
        requestBody<Unit> {
            multipart {
                part<PartData.FileItem>("file") {
                    description = "The file to upload."
                }
                part<PartData.FormItem>("metadata") {
                    description = "Metadata about the file, provided as JSON."
                }
            }
        }
        response<Uuid>(status = HttpStatusCode.Created) {
            description = "Document created."
        }
        bearerSecurity(name = "Authentication") {
            description = "Access to document storage."
        }
    }
}
```
