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
buildscript {
    repositories {
        jcenter()
        google()
        maven { url "https://dl.bintray.com/yshrsmz/kgql" }
    }
    dependencies {
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61'
        classpath 'org.jetbrains.kotlin:kotlin-serialization:0.14.0'
        classpath 'com.codingfeline.kgql:gradle-plugin:0.4.2'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlinx-serialization'
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
}
```

#### For Kotlin Multiplatform Project

```gradle
buildscript {
    repositories {
        jcenter()
        google()
        maven { url "https://dl.bintray.com/yshrsmz/kgql" }
    }
    dependencies {
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61'
        classpath 'org.jetbrains.kotlin:kotlin-serialization:0.14.0'
        classpath 'com.codingfeline.kgql:gradle-plugin:0.4.2'
    }
}

apply plugin: 'kotlin-multiplatform'
apply plugin: 'kotlinx-serialization'
apply plugin: 'com.codingfeline.kgql'

repositories {
     maven { url "https://dl.bintray.com/yshrsmz/kgql" }
}

kotlin {
    // kotlin configurations...
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

#### How to generate wrapper classes

When you apply kgql plugin, `generateKgqlInterface` task is added to the project. Manually executing it is one way, but the task is integrated into project's build task, so it will be generated upon each build.


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
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
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
            body = TextContent(text = ViewerDocument.Query.requestBody(), contentType = ContentType.Application.Json)

            headers {
                append("Authorization", "bearer $TOKEN")
            }
        }

        val res = JSON.parse(ViewerResponse.serializer(), response)

        return res.data?.viewer
    }
}

```

## Try out the sample

Have a look at `./sample` directory.

```
# Try out the samples.
# BuildKonfig will be generated in ./sample/build/kgql
$ ./gradlew -p sample generateKgqlInterface
```


## Credits

This library is highly inspired by [squareup/sqldelight](https://github.com/squareup/sqldelight) and the gradle plugin and basic idea is heavily based on it. Thanks for this.
