kgql
===

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codingfeline.kgql/gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codingfeline.kgql/gradle-plugin)

GraphQL Document wrapper generator for Kotlin Multiplatform Project.  
Currently, available for JVM/Android/iOS

## core

kgql core classes

## kgql-gradle-plugin

kgql Gradle Plugin generates wrapper classes for provided GraphQL document files.

### Setup

kgql requires Gradle __6.5 or later__

Supported GraphQL file extension: `.gql` or `.graphql`

#### For Android Project

```gradle
buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }
    dependencies {
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10'
        classpath 'org.jetbrains.kotlin:kotlin-serialization:1.2.1'
        classpath 'com.codingfeline.kgql:gradle-plugin:0.6.0'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlinx-serialization'
apply plugin: 'com.codingfeline.kgql'

repositories {
     mavenCentral()
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
        mavenCentral()
        google()
        jcenter()
    }
    dependencies {
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10'
        classpath 'org.jetbrains.kotlin:kotlin-serialization:1.2.1'
        classpath 'com.codingfeline.kgql:gradle-plugin:0.6.0'
    }
}

apply plugin: 'kotlin-multiplatform'
apply plugin: 'kotlinx-serialization'
apply plugin: 'com.codingfeline.kgql'

repositories {
     mavenCentral()
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

When you apply kgql plugin, `generateKgqlInterface` task is added to the project. Manually executing it is one way, but
the task is integrated into project's build task, so it will be generated upon each build.

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

object ViewerDocument {
    private val document: String = """
            |query {
            |  viewer {
            |    login
            |  }
            |}
            |""".trimMargin()

    object Query {
        /**
         * Create an instance of [Request] which then you can encode to JSON string
         */
        fun requestBody(): Request = Request()

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

As you can see, generated code utilizes data class's default value. So in order to properly serialize, you need to
set `encodeDefaults` to true in your `kotlinx.serialization.json.Json` instance.

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
) : KgqlResponse<ViewerWrapper>


class GitHubApi {

    private val json = Json {
        // encodeDefaults must be set to true
        encodeDefaults = true
    }

    private val client = HttpClient {
        install(JsonFeature) {
            this.serializer = KotlinxSerializer(json = json)
        }
    }

    suspend fun fetchLogin(): Viewer? {

        val body = json.encodeToString(ViewerDocument.Query.serializer(), ViewerDocument.Query.requestBody())
        val response = client.post<String>(url = Url("https://api.github.com/graphql")) {
            body = TextContent(text = body, contentType = ContentType.Application.Json)

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

### Try sample with snapshot

```
# Try out the samples.
# BuildKonfig will be generated in ./sample/build/kgql
$ ./gradlew clean build installArchives
$ ./gradlew -p sample generateKgqlInterface
```

## Credits

This library is highly inspired by [squareup/sqldelight](https://github.com/squareup/sqldelight) and the gradle plugin
and basic idea is heavily based on it. Thanks for this.
