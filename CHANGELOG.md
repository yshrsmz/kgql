Change Log
===

Badges: `[UPDATED]`, `[FIXED]`, `[ADDED]`, `[DEPRECATED]`, `[REMOVED]`,  `[BREAKING]`

Unreleased
---
* `[UPDATED]`: rewrite plugin


Version 0.5.3 *(2020-08-25)*
---
* `[UPDATED]`: Kotlin 1.4.0
* `[UPDATED]`: Android Gradle Plugin 4.0.1
* `[UPDATED]`: Gradle 6.6
* `[REMOVED]`: iosArm32 is gone again
* `[BREAKING]`: New JS IR backend


Version 0.5.2 *(2020-04-13)*
---
* `[UPDATED]`: Android Gradle Plugin 3.6.2
* `[UPDATED]`: Project Gradle Version is now 6.3
* `[UPDATED]`: Use maven-publish plugin to publish jvm artifacts
* `[ADDED]`: iosArm32 artifact is back
* `[FIXED]`: Remove unnecessary `@UnstableDefault` annotation.

Version 0.5.1 *(2020-04-01)*
---
* `[UPDATED]`: Kotlin 1.3.71
* `[UPDATED]`: kotlinx.serialization 0.20.0
* `[UPDATED]`: Android Gradle Plugin 3.6.1
* `[UPDATED]`: Gradle 5.6.4
* `[BREAKING]`: drop support for iosArm32
* `[BREAKING]`: `requestBody` method now requires `Json` instance, due to the changes in kotlinx.serialization


Version 0.4.2 *(2019-12-07)*
---
* `[UPDATED]`: Kotlin 1.3.61


Version 0.4.0 *(2019-09-02)*
---

* `[UPDATED]`: Kotlin 1.3.50
* `[UPDATED]`: kotlinx.serialization 0.12.0
* `[UPDATED]`: Android Gradle Plugin 3.5.0
* `[UPDATED]`: Android target SDK version 29
* `[UPDATED]`: Gradle 5.6.1
* `[ADDED]`: `KgqlError` now provides other GraphQL error fields.


Version 0.3.2 *(2019-08-13)*
---

* `[FIXED]`: ID type is converted to Any [#27](https://github.com/yshrsmz/kgql/issues/27)


Version 0.3.1 *(2019-07-12)*
---

* `[UPDATED]`: Kotlin 1.3.41
* `[UPDATED]`: kotlinx.serialization 0.11.1
* `[UPDATED]`: graphql-java 13.0
* `[UPDATED]`: Android Gradle Plugin 3.4.2
* `[ADDED]`: Support `.graphql` file extension
* `[ADDED]`: Add UnstableDefault annotation
* `[ADDED]`: `requestBody` method now optionally take `kotlinx.serialization.json.Json` instance to customize serialization behavior


Version 0.2.2 *(2019-04-18)*
---

* `[UPDATED]`: Kotlin 1.3.30
* `[UPDATED]`: kotlinx.serialization 0.11.0
* `[UPDATED]`: Android Gradle Plugin 3.4.0
* `[UPDATED]`: graphql-java 12.0


Version 0.2.1 *(2019-02-12)*
---

* `[ADDED]`: Support iOS Arm32
* `[UPDATED]`: Kotlin 1.3.21
* `[UPDATED]`: Android Gradle Plugin 3.3.1


Version 0.2.0 *(2019-02-06)*
---

* `[UPDATED]`: Rewrite Plugin in Kotlin ([#15](https://github.com/yshrsmz/kgql/issues/15))
* `[UPDATED]`: Applying plugin in Android project now automatically add `core-jvm` dependency.


Version 0.1.1 *(2019-02-05)*
---

* `[UPDATED]`: Replace `println` with `Logger`


Version 0.1.0 *(2019-01-28)*
---

* `[BREAKING]`: Generated Document Objects are now `internal` by default ([#13](https://github.com/yshrsmz/kgql/issues/13))
* `[FIXED]`: Fix generated file's output directory not correct.
* `[UPDATED]`: Kotlin 1.3.20 ([#14](https://github.com/yshrsmz/kgql/issues/14))
* `[UPDATED]`: Gradle 5.1.1 ([#14](https://github.com/yshrsmz/kgql/issues/14)) and __5.1.x or later__ is required.
* `[UPDATED]`: Android Gradle Plugin 3.3.0


Version 0.0.7 *(2019-01-22)*
---

* `[ADDED]`: Use `kotlinx.serialization.SerialName` annotation ([#11](https://github.com/yshrsmz/kgql/issues/11))
* `[FIXED]`: Android compilation task now depends on `generateKgqlInterface` task properly in Kotlin multiplatform project ([#12](https://github.com/yshrsmz/kgql/issues/12))
* `[FIXED]`: Gradle Plugin now depends on antlr4 to avoid dependency conflict with Android DataBinding


Version 0.0.6 *(2019-01-21)*
---

* `[BREAKING]`: Create dedicated object for each operation in a document ([#3](https://github.com/yshrsmz/kgql/issues/3))
* `[BREAKING]`: Change KgqlRequestBody & KgqlResponse to interface ([#8](https://github.com/yshrsmz/kgql/issues/8))
* `[ADDED]`: Change suffix of generate classes to Document, from DocumentWrapper
* `[FIXED]`: Downgrade to Kotlin 1.3.11 ([#8](https://github.com/yshrsmz/kgql/issues/8))


Version 0.0.5 *(2019-01-15)*
---

* `[ADDED]`: Release via `gradle-mvn-mpp-push.gradle`


Version 0.0.4 *(2019-01-14)*
---

* `[FIXED]`: Fix dependency resolution



Version 0.0.3 *(2019-01-14)*
---

* Initial preview release
