### Path Operation Tags

For path operations, tags can be defined to categorize and group related endpoints.

If any of the tags are not previously defined at top level in the plugin configuration,
these will be automatically merged to the top-level list, but without descriptions.

```kotlin
api {
    tags = setOf("Items", "Inventory")
    tags = setOf("AnotherTag")
    tags = setOf("AndMoreItems", "AndMoreInventory")
}
```

---

### [Parameters 🡲](04-parameters.md)

#### [🡰 Summary and Description](02-summary-description.md)
