kgql
===

GraphQL Document wrapper generator for Kotlin Multiplatform Project.  
Currently available for JVM/Android/iOS

## core



## kgql-gradle-plugin

kgql Gradle Plugin generates wrapper classes for provided GraphQL document files.


### Setup

```gradle
buildScript {
    repositories {
        jcenter()
        maven { url "https://dl.bintray.com/yshrsmz/kgql" }
    }
    dependencies {
        classpath 'com.codingfeline.kgql:gradle-plugin:0.0.3'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'com.codingfeline.kgql'

repositories {
     maven { url "https://dl.bintray.com/yshrsmz/kgql" }
}

dependencies {
    implementation "com.codingfeline.kgql:core:0.0.3"
}

kgql {
    packageName = "com.sample"
    sourceSet = files("src/main/kgql")
    typeMapper = [
        // mapper for non-scalar type
        "UserProfile": "com.sample.data.UserProfile"
    ]
}
```

## ktor-kgql

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
import kotlinx.serialization.KSerializer

object ViewerDocumentWrapper {
    private val document: String = """
            |query {
            |  viewer {
            |    login
            |  }
            |}
            |""".trimMargin()

    fun query(): KgqlRequestBody<Unit> = KgqlRequestBody<Unit>(
        operationName = null, query = document,
        variables = null
    )

    fun querySerializer(): KSerializer<KgqlRequestBody<Unit>> =
        KgqlRequestBody.serializer(kotlinx.serialization.internal.UnitSerializer)
}

```

You can use this code with Ktor or any other HttpClient.

Example usage with Ktor is below

```kotlin
package com.sample

import com.codingfeline.kgql.core.KgqlResponse
import com.sample.TestDocumentWrapper
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


class GitHubApi {

    private val client = HttpClient {
        install(JsonFeature)
    }

    suspend fun fetchLogin(): Viewer? {
        val query = TestDocumentWrapper.query()
        val serializer = TestDocumentWrapper.querySerializer()

        val response = client.post<String>(url = Url("https://api.github.com/graphql")) {
            body = JSON.stringify(serializer, query)

            headers {
                append("Authorization", "bearer $TOKEN")
            }
        }

        val res = JSON.parse(KgqlResponse.serializer(ViewerWrapper.serializer()), response)

        return res.data?.viewer
    }
}

```
