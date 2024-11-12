## Adding Security Schemes to API Operations (endpoints)

In addition to defining top-level security schemes in the plugin's main configuration,
it is also possible to define security schemes at the individual API Operation level,
allowing for more specific security configurations.

- For detailed information on the security schemes supported at the API Operation level,
  see the plugin [Security](../01-plugin/03-security.md) section, as the syntax is the same.

---

### Key Points

- **Scheme Uniqueness:**
  - Scheme names should be unique across both top-level and API Operation security schemes.
  - Only `OAuth2` API Operation-level schemes should share the same name as a top-level scheme for `scope` overrides.

- **Overriding Top-Level Schemes:**
  - Only `OAuth2` schemes support overriding at the operation level, but only `scopes` can be overridden.
  - For `OAuth2` all other properties (like `authorizationUrl`, `tokenUrl`, etc.) should match the top-level scheme or be omitted.
  - `Non-OAuth2` schemes (e.g., `HTTP`, `API Key`), do not support overriding.
  - If `Non-OAuth2` schemes share same name between different API Operations, they should fully match in all properties.

- **Combining Top-Level and Operation-Level Schemes:**
  - For `OAuth2`, should schemes are applied automatically unless an API Operation-level scheme overrides the `scopes`.
  - For `non-OAuth2` schemes, top-level and operation-level schemes should not coexist with the same name.

- **Scope Overrides:**
  - Only `OAuth2` schemes allow `scope` redefinition at the operation level.
  - `Non-OAuth2` schemes do not support partial overrides, so should be either defined at top-level, or only at API Operation-level.

---

### Overriding Example

```kotlin
// Assuming OAuth2 has been defined as top-level in the plugin configuration.
oauth2Security(name = "MyOAuth2Scheme") {
  // Override only the scopes for this API Operation.
  authorizationCode {
    scope(name = "read:users")
    scope(name = "write:users")
  }
}
```

- In this case, only the `scopes` are overridden. All other properties (e.g., `authorizationUrl`, `tokenUrl`, etc.) are inherited.
- If re-defining any of the properties, these should match the top-level scheme.

---

### Disable Security

Use the `noSecurity()` method to disable security for an API Operation, ensuring that no top-level or operation-level
security schemes are applied to that API Operation.

```kotlin
noSecurity()
```

- Behavior: If `noSecurity()` is used along with other security schemes in the same API Operation,
  all other schemes are ignored, and no security is applied to the API Operation.

---

### Non-OAuth2 Schemes

For schemes such as `HTTP`, `API Key`, `OpenIdConnect` and `Mutual TLS`, overriding at the operation level is not supported.
If you need to define a different scheme at the operation level, you should assign a unique name to the scheme and fully define it,
as partial redefinition is not supported.

---

### [Schema Annotation 🡲](08-servers.md)

#### [🡰 Responses](06-responses.md)
