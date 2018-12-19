kgql-dsl
===

```kotlin
val query = query("operationName") {
    field("user", 
        args = setOf("id" to 4)) {
        
        field("id")
        field("name")
    }
}

query.toDocument()
```

```
query operationName {
    user(id: 4) {
        id
        name
    }
}
```
