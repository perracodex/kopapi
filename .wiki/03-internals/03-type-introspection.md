## Introspecting Kotlin Types for OpenAPI Schemas

This section explains the internal mechanisms of type introspection used to extract information for generating OpenAPI schemas.
It describes the core components, type decisions, and traversals involved in this process.

### Key Features:

* Recursive Introspection: Capable of handling complex types, including nested and self-referencing objects.
* Comprehensive Type Support: Processes primitive types, enums, iterables, maps, arrays, and generic types.
* Annotation Support: Recognizes annotations from kotlinx serialization. Supports a subset of Jackson annotations.
* Caching Mechanism: Caches resolved schemas to improve performance and avoid redundant processing.
* Conflict Detection: Identifies conflicts where different types may have the same name.

---

### Core Components

Before diving into the flow, it's essential to understand the primary components involved in the type introspection process:

- `TypeSchemaProvider`: The entry point that initiates the introspection process for a given type.
- `TypeIntrospector`: The core orchestrator delegating to specific resolvers based on the type characteristics.
    - `Resolvers`: Specialized components that handle different kinds of types:
    - `ArrayResolver`: Handles array types.
    - `IterableResolver`: Handles iterable types like `List` and `Set`, including typed arrays `Array<T>`.
    - `MapResolver`: Handles `map` types.
    - `EnumResolver`: Handles enumeration types.
    - `GenericsResolver`: Handles `generics` types with type parameters.
    - `ObjectResolver`: Handles complex object types, such as classes and data classes.
    - `PropertyResolver`: Handles the properties of complex types, extracting metadata and resolving property types.

---

### High-Level Flow

The introspection process involves initiating the introspection of a given Kotlin type
and recursively traversing its structure to build the corresponding schema.
The flow includes decision-making to determine the appropriate resolver for each type
and may involve recursion to handle nested types.

### Main Introspection Entry Flow

```text
Start
 |
 v
TypeSchemaProvider initiates introspection of Kotlin type
 |
 v
TypeIntrospector checks the type characteristics and delegates to appropriate resolvers
```

* `TypeSchemaProvider`: Serves as the entry point, starting the introspection for a given type.
* `TypeIntrospector`: Analyzes the type and decides which resolver should handle it based on its characteristics.

---

### TypeIntrospector Decision Tree

The `TypeIntrospector` uses a decision tree to determine how to process a given type.
This tree guides the traversal and ensures that each type is handled by the appropriate resolver.

```text
traverseType:
  |
  +-> Is the type an Array?
  |    |
  |    +-> Yes:
  |        - Is it a Primitive Array?
  |        |   |
  |        |   +-> Yes:
  |        |   |       - Map the primitive array type to schema.
  |        |   |       - Return schema.
  |        |   |
  |        |   +-> No:
  |        |       - Is it a typed array `Array<T>`?
  |        |           |
  |        |           +-> Yes:
  |        |           |       - Delegate to `IterableResolver`.
  |        |           |
  |        |           +-> No:
  |        |                   - Log error.
  |        |                   - Return unknown object type schema.
  |        |
  |        +-> No: Skip to next decision check.
  |
  +-> Is the type an Iterable?
  |    |
  |    +-> Yes:
  |    |       - `IterableResolver` processes the type.
  |    |       - Resolve the element type.
  |    |       - Traverse the element type using `TypeIntrospector`.
  |    |       - Build iterable schema.
  |    |       - Return schema.
  |    |
  |    +-> No: Skip to next decision check.
  |
  +-> Is the type a Map?
  |     |
  |     +-> Yes:
  |     |      - `MapResolver` processes the type.
  |     |      - Validate that the key type is String.
  |     |      - Resolve the value type.
  |     |      - Traverse the value type using `TypeIntrospector`.
  |     |      - Build map schema with `additionalProperties`.
  |     |      - Return schema.
  |     |
  |     +-> No: Skip to next decision check.
  |
  +-> Is the type an Enum?
  |     |
  |     +-> Yes:
  |     |      - `EnumResolver` processes the type.
  |     |      - If not cached:
  |     |          - Extract enum values.
  |     |          - Build enum schema.
  |     |          - Add schema to cache.
  |     |      - Return schema or reference to schema.
  |     |
  |     +-> No: Skip to next decision check.
  |
  +-> Does the type have Type Arguments? (Generics)
  |    |
  |    +-> Yes:
  |    |       - `GenericsResolver` processes the type.
  |    |       - Generate unique name for the generic type.
  |    |       - If not cached:
  |    |           - Map type arguments to actual types.
  |    |           - Merge type argument bindings from outer context.
  |    |           - Traverse properties:
  |    |              - For each property:
  |    |                - Traverse property type using `TypeIntrospector`.
  |    |           - Build schema.
  |    |           - Add schema to cache.
  |    |       - Return schema or reference to schema.
  |    |
  |    +-> No: Skip to next decision check.
  |
  +-> Is the type a KClass? (Object)
      |
      +-> Yes:
      |       - `ObjectResolver` processes the type.
      |       - Check if the type is cached.
      |       - If not cached:
      |           - Add placeholder to cache.
      |           - Retrieve properties.
      |           - Traverse properties:
      |              - For each property:
      |                - Traverse property type using `TypeIntrospector`.
      |       - Build schema.
      |          - Update cache.
      |       - Return schema or reference to schema.
      |
      +-> No:
          - Log error.
          - Return unknown object type schema.
```

* Delegation: At each decision point, the `TypeIntrospector` either delegates to a specific resolver
  or constructs a default schema if the type cannot be processed.

---

### Detailed Flow

#### Starting Introspection with TypeSchemaProvider

- Initiation: The `TypeSchemaProvider` starts the introspection process for the given Kotlin type.
- Conflict Analysis: After the traversal, it analyzes for any conflicts, such as schemas with the same name but different types.
- Result: The `TypeSchemaProvider` returns the resolved `TypeSchema`.

#### Traversal with TypeIntrospector

- Classifier Determination: The `TypeIntrospector` examines the type to determine its nature (e.g., array, iterable, map).
- Decision-Making: Based on the decision tree, it determines which resolver is appropriate for the type.
- Delegation: It delegates the processing to the chosen resolver.
- Recursion: If necessary, the `TypeIntrospector` uses recursion to handle nested types.

#### Resolvers Interaction and Processing

- **ArrayResolver**
    - Purpose: Handles array types, differentiating between primitive arrays and typed arrays `Array<T>`.
    - Process:
        - Determines if the array is a primitive array (e.g., `IntArray`).
            - If yes, processes it immediately and constructs the schema.
      - If not a primitive array, checks if it's a typed array `Array<T>`.
          - If yes, delegates processing to the `IterableResolver`.
              - If neither, constructs an unknown object type schema.

- **IterableResolver**
    - Purpose: Handles iterable types like `List` and `Set`.
    - Process:
        - Resolves the element type of the iterable.
      - Traverses the element type using the `TypeIntrospector`, which may involve recursion.
          - Constructs the iterable schema using the element schema.

- **MapResolver**
    - Purpose: Handles Map types.
    - Process:
        - Validates that the map's key type is `String` (as required by OpenAPI).
        - Resolves the value type of the map.
      - Traverses the value type using the `TypeIntrospector`.
          - Constructs the map schema with `additionalProperties` representing the value schema.

- **EnumResolver**
    - Purpose: Handles enumeration types.
    - Process:
        - Extracts the list of possible `enum` values.
        - Constructs the `enum` schema.
        - Adds the schema to the cache if not already present.
        - Returns the schema or a reference to it.

- **GenericsResolver**
    - Purpose: Handles generic types with type parameters.
    - Process:
        - Generates a unique name for the generic type (e.g., `PageOfEmployee`).
        - If not already cached:
            - Creates a local type argument bindings, mapping generic type parameters to actual types.
            - Merges the local map with any inherited type argument bindings.
            - Traverses each property of the generic type:
                - Uses the `TypeIntrospector` to traverse property types, substituting type parameters as necessary.
            - Constructs the schema for the generic type.
            - Adds the schema to the cache.
        - Returns the schema or a reference to it.

- **ObjectResolver**
    - Purpose: Handles complex object types, such as classes and data classes.
    - Process:
        - Checks if the type is a primitive type.
            - If yes, maps it directly to its schema representation.
        - If not, handles it as a complex type:
            - Uses a semaphore to prevent infinite recursion in case of self-referencing types.
            - Adds a placeholder schema to the cache before processing properties.
            - Retrieves the properties of the object using the PropertyResolver.
          - Traverses each property recursively using the `TypeIntrospector`.
              - Updates the placeholder schema with the resolved properties.
              - Removes the type from the semaphore after processing.
        - Returns the schema or a reference to it.

- **PropertyResolver**
    - Purpose: Handles the properties of complex types, extracting metadata and resolving property types.
    - Process:
        - Retrieves the properties of the class, preserving their declaration order.
            - Includes properties from primary constructors and class bodies.
            - Excludes non-public properties.
        - For each property:
            - Extracts metadata such as name, nullability, and annotations.
            - Resolves the property's type, substituting generics as necessary.
          - Traverses the property's type using the `TypeIntrospector`.
              - Applies metadata to the property's schema.
              - Collects property schemas to be included in the parent object's schema.

#### Handling Generics and Type Arguments

- **Type Argument Bindings:**
    - Purpose: Keeps track of substitutions for generic type arguments during traversal.
    - Behavior:
        - Isolation: Each traversal context maintains its own type argument bindings to prevent interference between different contexts.
        - Propagation: Passed down during recursive traversal with each traversal context to ensure correct substitutions at each level.
    - Usage:
        - When traversing a generic type, local bindings are created, mapping type arguments to actual types.
        - Used during traversal to substitute generic arguments with actual types.
    - Flow:
        - When introspecting `Container<T>`, and `T` is mapped to `SomeType`,
          the map `{ T -> SomeType }` is used to substitute `T` with `SomeType`.

- **Example:** introspecting `Page<Employee>`
    - Scenario:
        - `Page<T>` is a generic class with a type parameter `T`.
      - We are introspecting `Page<Employee>`, where `T` is `Employee`.
    - Process:
        - The `TypeIntrospector` recognizes `Page` as a generic type with type parameter `T`.
      - Creates a type argument bindings `{ T -> Employee }`.
          - Traverses properties of `Page<T>`:
              - For the property content: `T`, substitutes `T` with `Employee`.
              - Traverses `Employee` recursively.
    - Constructs the schema for `PageOfEmployee`, referencing the `Employee` schema.

#### Caching and Circular References

- **Caching:**
    - Purpose: Avoids redundant processing by storing resolved schemas.
    - Mechanism:
        - Before processing a type, checks if it's already in the cache.
        - If cached, uses the existing schema reference.
        - Adds new schemas to the cache after processing.
    - Benefit: Enhances performance and efficiency, especially with recursive or shared types.

- ** Circular Reference Handling:**
    - Purpose: Prevents infinite recursion when types reference themselves directly or indirectly.
    - Mechanism:
        - Uses a semaphore to track types currently being processed.
        - Adds a placeholder schema to the cache before traversing properties.
        - Updates the placeholder schema after traversal is complete.
        - Removes the type from the semaphore after processing.
    - Behavior:
        - When a class has a property of its own type, or any of its nested properties is of the same type,
          the semaphore detects this self-reference, prevents infinite recursion, and returns a self-reference
          scheme while the type is still being processed, ensuring proper handling of deeply nested structures.

---

### Example Flows

#### Introspecting a typed Array: `Array<Employee>`

1. `TypeIntrospector`:
    - Determines the type is an array (either primitive or typed).
    - Delegates to `ArrayResolver`.
2. `ArrayResolver`:
    - Identifies if the array is a primitive array.
        - It's not.
    - Checks if it's a typed array `Array<T>`.
        - It is.
        - Delegates to `IterableResolver`.
3. `IterableResolver`:
    - Resolves the Element Type: `Employee`.
   - Traverses the Element Type using `TypeIntrospector`.
4. `TypeIntrospector` (recursive call):
    - Determines the type is an Object `Employee`.
    - Delegates to `ObjectResolver`.
5. `ObjectResolver`:
    - Checks if `Employee` is already cached.
        - If not, adds a placeholder to the cache.
    - Retrieves properties of `Employee` using `PropertyResolver`.
   - Traverses each property, invoking `TypeIntrospector `recursively.
    - Updates the `Employee` schema in the cache.
6. `IterableResolver`:
    - Constructs the schema for `Array<Employee>` using the `Employee` schema.
7. `ArrayResolver`:
    - Returns the schema for `Array<Employee>`.
8. `TypeIntrospector`:
    - Returns the schema for `Array<Employee>`.

#### Introspecting an Iterable: `List<Employee>`

1. `TypeIntrospector`:
    - Determines the type is an Iterable.
    - Delegates to `IterableResolver`.
2. `IterableResolver`:
    - Resolves the Element Type: `Employee`.
   - Traverses the Element Type using `TypeIntrospector`.
3. `TypeIntrospector` (recursive call):
    - Determines the type is an Object (`Employee`).
    - Delegates to `ObjectResolver`.
4. `ObjectResolver`:
    - Checks if `Employee` is already cached.
        - If not, adds a placeholder to the cache.
    - Retrieves properties of `Employee` using `PropertyResolver`.
   - Traverses each property, invoking `TypeIntrospector `recursively.
    - Updates the `Employee` schema in the cache.
5. `IterableResolver`:
    - Constructs the schema for `List<Employee>` using the `Employee` schema.
6. `TypeIntrospector`:
    - Returns the schema for `List<Employee>`.

#### Introspecting a Map: `Map<String, Employee>`

1. `TypeIntrospector`:
    - Determines the type is a Map.
    - Delegates to `MapResolver`.
2. `MapResolver`:
    - Validates the key type is `String`.
    - Resolves the value type: `Employee`.
   - Traverses the value type using `TypeIntrospector`.
3. `TypeIntrospector` (recursive call):
    - Determines the type is an Object `Employee`.
    - Delegates to `ObjectResolver`.
4. `ObjectResolver`:
    - Checks if `Employee` is already cached.
        - If not, adds a placeholder to the cache.
    - Retrieves properties of `Employee` using `PropertyResolver`.
   - Traverses each property, invoking `TypeIntrospector` recursively.
    - Updates the `Employee` schema in the cache.
5. `MapResolver`:
    - Constructs the schema for `Map<String, Employee>` using the `Employee` schema.
6. `TypeIntrospector`:
    - Returns the schema for `Map<String, Employee>`.

---

### [🡰 Internals - Type Conflicts](02-type-conflicts.md)
