kgql
===

GraphQL Client for Kotlin MPP

## core

## kgql-gradle-plugin

kgql Gradle Plugin generates wrapper classes for provided GraphQL document files.


### Setup

```gradle
buildScript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.codingfeline.kgql:gradle-plugin:0.0.1'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'com.codingfeline.kgql'

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



