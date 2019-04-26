kgql
===

[ ![Download](https://api.bintray.com/packages/yshrsmz/kgql/gradle-plugin/images/download.svg) ](https://bintray.com/yshrsmz/kgql/gradle-plugin/_latestVersion)

GraphQL Document wrapper generator for Kotlin Multiplatform Project.  
Currently available for JVM/Android/iOS

## core

kgql core classes


## kgql-gradle-plugin

kgql Gradle Plugin generates wrapper classes for provided GraphQL document files.


### Setup

kgql requires Gradle __5.3.1 or later__

Supported GraphQL file extension: `.gql` or `.graphql`

#### For Android Project

```gradle
buildScript {
    repositories {
        jcenter()
        maven { url "https://dl.bintray.com/yshrsmz/kgql" }
    }
    dependencies {
        classpath 'com.codingfeline.kgql:gradle-plugin:0.3.1'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'com.codingfeline.kgql'

repositories {
     maven { url "https://dl.bintray.com/yshrsmz/kgql" }
}

kgql {
    packageName = "com.sample"
    sourceSet = files("src/main/kgql")
    typeMapper = [
        // mapper for non-scalar type
        "UserProfile": "com.sample.data.UserProfile"
    ]
    schemaJson = file("src/main/kgql/schema.json) // defaults to "src/main/kgql"
}
```

#### For Kotlin Multiplatform Project

```gradle
buildScript {
    repositories {
        jcenter()
        maven { url "https://dl.bintray.com/yshrsmz/kgql" }
    }
    dependencies {
        classpath 'com.codingfeline.kgql:gradle-plugin:0.3.1'
    }
}

apply plugin: 'kotlin-multiplatform'
apply plugin: 'com.codingfeline.kgql'

repositories {
     maven { url "https://dl.bintray.com/yshrsmz/kgql" }
}

kgql {
    packageName = "com.sample"
    sourceSet = files("src/main/kgql") // defaults to "src/main/kgql"
    typeMapper = [
        // mapper for non-scalar type
        "UserProfile": "com.sample.data.UserProfile"
    ]
    schemaJson = file("src/main/kgql/schema.json) // defaults to "src/main/kgql"
}
```


## [WIP]kgql-ktor

ktor extensions for kgql



## How it works

```
# viewer.gql
query {
  viewer {
    login
  }
}
```

Below code will be generated from above GraphQL document file(viewer.gql).

```kotlin
package com.sample

import com.codingfeline.kgql.core.KgqlRequestBody
import kotlin.String
import kotlin.Unit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

object ViewerDocument {
    private val document: String = """
            |query {
            |  viewer {
            |    login
            |  }
            |}
            |""".trimMargin()

    @UnstableDefault
    object Query {
        /**
         * Generate Json string of [Request]
         */
        fun requestBody(json: Json = Json.plain): String = json.stringify(serializer(), Request())

        fun serializer(): KSerializer<Request> = Request.serializer()

        @Serializable
        data class Request(
            @SerialName(value = "variables") @Optional override val variables: Unit? = null,
            @Optional @SerialName(value = "operationName") override val operationName: String? =
                    null,
            @SerialName(value = "query") override val query: String = document
        ) : KgqlRequestBody<Unit>
    }
}
```

You can use this code with Ktor or any other HttpClient.

Example usage with Ktor is below

```kotlin
package com.sample

import com.codingfeline.kgql.core.KgqlResponse
import com.codingfeline.kgql.core.KgqlError
import com.sample.ViewerDocument
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.Url
import kotlinx.serialization.json.JSON
import kotlinx.serialization.Serializable

const val TOKEN = "YOUR_GITHUB_TOKEN"

@Serializable
data class ViewerWrapper(
    val viewer: Viewer
)

@Serializable
data class Viewer(
    val login: String
)

@Serializable
data class ViewerResponse(
    override val data: ViewerWrapper?,
    override val errors: List<KgqlError>?
): KgqlResponse<ViewerWrapper>


class GitHubApi {

    private val client = HttpClient {
        install(JsonFeature)
    }

    suspend fun fetchLogin(): Viewer? {

        val response = client.post<String>(url = Url("https://api.github.com/graphql")) {
            body = ViewerDocument.Query.requestBody()

            headers {
                append("Authorization", "bearer $TOKEN")
            }
        }

        val res = JSON.parse(ViewerResponse.serializer(), response)

        return res.data?.viewer
    }
}

```


## Introspection Query

```graphql
query IntrospectionQuery {
  __schema {
    queryType { name }
    mutationType { name }
    subscriptionType { name }
    types {
      ...FullType
    }
    directives {
      name
      description
      locations
      args {
        ...InputValue
      }
    }
  }
}

fragment FullType on __Type {
  kind
  name
  description
  fields(includeDeprecated: true) {
    name
    description
    args {
      ...InputValue
    }
    type {
      ...TypeRef
    }
    isDeprecated
    deprecationReason
  }
  inputFields {
    ...InputValue
  }
  interfaces {
    ...TypeRef
  }
  enumValues(includeDeprecated: true) {
    name
    description
    isDeprecated
    deprecationReason
  }
  possibleTypes {
    ...TypeRef
  }
}

fragment InputValue on __InputValue {
  name
  description
  type { ...TypeRef }
  defaultValue
}

fragment TypeRef on __Type {
  kind
  name
  ofType {
    kind
    name
    ofType {
      kind
      name
      ofType {
        kind
        name
        ofType {
          kind
          name
          ofType {
            kind
            name
            ofType {
              kind
              name
              ofType {
                kind
                name
              }
            }
          }
        }
      }
    }
  }
}
```

## Credits

This library is highly inspired by [squareup/sqldelight](https://github.com/squareup/sqldelight) and the gradle plugin and basic idea is heavily based on it. Thanks for this.
