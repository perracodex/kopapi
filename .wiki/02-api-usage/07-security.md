## Adding Security Schemes to API Operations (endpoints)

In addition to defining global security schemes in the plugin's main configuration,
it is also possible to define security schemes at the individual API Operation level,
allowing for more specific security configurations.

- For detailed information on the security schemes supported at the API Operation level,
  see the plugin [Security](../01-plugin/03-security.md) section, as the syntax is the same.

---

### Key Points

- **Scheme Uniqueness:**
  - Scheme names must be unique across both global and API Operation security schemes.
  - Only `OAuth2` API Operation-level schemes can share the same name as a global scheme for `scope` overrides.

- **Overriding Global Schemes:**
  - Only `OAuth2` schemes support overriding at the operation level, but only `scopes` can be overridden.
  - For `OAuth2` all other properties (like `authorizationUrl`, `tokenUrl`, etc.) must match the global scheme or be omitted.
  - `Non-OAuth2` schemes (e.g., `HTTP`, `API Key`), do not support overriding.
  - If `Non-OAuth2` schemes share same name between different API Operations, they must fully match in all properties.

- **Combining Global and Operation-Level Schemes:**
  - For `OAuth2`, global schemes are applied automatically unless an API Operation-level scheme overrides the `scopes`.
  - For `non-OAuth2` schemes, global and operation-level schemes cannot coexist with the same name.

- **Scope Overrides:**
  - Only `OAuth2` schemes allow `scope` redefinition at the operation level.
  - `Non-OAuth2` schemes do not support partial overrides, so should be either defined at global level, or only at API Operation-level.

---

### Overriding Example

```kotlin
// Assuming OAuth2 has been defined globally at the plugin level.
oauth2Security(name = "MyOAuth2Scheme") {
  // Override only the scopes for this API Operation.
  authorizationCode {
    scope(name = "read:users")
    scope(name = "write:users")
  }
}
```

- In this case, only the `scopes` are overridden. All other properties (e.g., `authorizationUrl`, `tokenUrl`, etc.) are inherited.
- If re-defining any of the properties, these must match the global scheme.

---

### Disable Security

Use the `skipSecurity()` method to disable security for an API Operation, ensuring that no global or operation-level security schemes
are applied to that API Operation.

```kotlin
skipSecurity()
```

- Behavior: If `skipSecurity()` is used along with other security schemes in the same API Operation,
  all other schemes are ignored, and no security is applied to the API Operation.

---

### Non-OAuth2 Schemes

For schemes such as `HTTP`, `API Key`, `OpenIdConnect` and `Mutual TLS`, overriding at the operation level is not supported.
If you need to define a different scheme at the operation level, you must assign a unique name to the scheme and fully define it,
as partial redefinition is not supported.

---

### [Schema Annotation 🡲](08-schema-annotation.md)

#### [🡰 Responses](06-responses.md)