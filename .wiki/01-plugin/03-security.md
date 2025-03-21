## Adding Security Schemes

Top-level security schemes can be defined in the plugin main configuration.

- These schemes are applied to all endpoints by default.
- These can be overridden per endpoint if needed.
- Scheme names must be unique across the top-level and endpoint security schemes.

---

### HTTP Schemes

```kotlin
basicSecurity(name = "MyBasicAuth") {
    description = "Basic Authentication"
}

bearerSecurity(name = "MyBearerAuth") {
  description = "Bearer Authentication"
}

digestSecurity(name = "MyDigestAuth") {
  description = "Digest Authentication"
}
```

- **Properties:**
    - `name` The unique name of the security scheme
    - `description`: A description of the security scheme.

---

### API Key Schemes

```kotlin
headerApiKeySecurity(name = "My Header API Key", key = "X-API-Key") {
    description = "API Key Authentication via header."
}

queryApiKeySecurity(name = "My Query API Key", key = "X-API-Key") {
  description = "API Key Authentication via query."
}

cookieApiKeySecurity(name = "My Cookie API Key", key = "X-API-Key") {
  description = "API Key Authentication via cookie."
}
```

- **Properties:**
    - `name`: The unique name of the security scheme.
  - `key`: The name of the header, query, or cookie parameter where the API key is passed.
    - `description`: A description of the security scheme.

---

### OAuth2 Scheme

```kotlin
oauth2Security(name = "OAuth2") {
    description = "OAuth2 Authentication."

    authorizationCode {
        authorizationUrl = "https://example.com/auth"
        tokenUrl = "https://example.com/token"
        refreshUrl = "https://example.com/refresh"
        scope(name = "read:employees", description = "Read Data")
        scope(name = "write:employees", description = "Modify Data")
    }

    clientCredentials {
        tokenUrl = "https://example.com/token"
        scope(name = "admin:tools", description = "Administrate Tools")
    }

    implicit {
        authorizationUrl = "https://example.com/auth"
        scope(name = "view:projects", description = "View Projects")
    }

    password {
        tokenUrl = "https://example.com/token"
        scope(name = "access:reports", description = "Access Reports")
    }
}
```

- **Properties:**
    - `name`: The unique name of the security scheme.
    - `description`: A description of the security scheme.
  - `authorizationCode`, `clientCredentials`, `implicit`, `password`: Each block configures a specific OAuth2 flow.
    - `authorizationUrl`: The authorization URL.
    - `tokenUrl`: The token URL.
    - `refreshUrl`: The refresh URL.
      - `scope`: A list of scopes available for the OAuth2 flow.

---

### OpenID Connect Scheme

```kotlin
openIdConnectSecurity(name = "OpenID") {
    description = "OpenID Connect Authentication."
    url = Url("https://example.com/.well-known/openid-configuration")
}
```

- **Properties:**
    - `name`: The unique name of the security scheme.
    - `description`: A description of the security scheme.
    - `url`: The URL to the OpenID Connect configuration.

---

### Mutual TLS Scheme

```kotlin
mutualTLSSecurity(name = "MutualTLS") {
    description = "Mutual TLS Authentication."
}
```

- **Properties:**
    - `name`: The unique name of the security scheme.
    - `description`: A description of the security scheme.

---

### Example

```kotlin
install(Kopapi) {

    // ...
    // Security schemes.
    bearerSecurity(name = "BearerAuth") {
        description = "HTTP bearer Authentication is required."
    }

    basicSecurity(name = "BasicAuth") {
        description = "HTTP basic Authentication is required."
    }

    oauth2Security(name = "OAuth2") {
        description = "OAuth2 Authentication is required."
    }
}
```

---

### [Top Level Tags 🡲](04-tags.md)

#### [🡰 Servers](02-servers.md)
