Change Log
===

Badges: `[UPDATED]`, `[FIXED]`, `[ADDED]`, `[DEPRECATED]`, `[REMOVED]`,  `[BREAKING]`

Version 0.11.0 *(2022/09/09)*
---

* `[UPDATED]`: Kotlin 1.7.10
* `[UPDATED]`: Gradle wrapper 7.5.1
* `[UPDATED]`: Android Gradle Plugin 7.2.2
* `[UPDATED]`: Android compile sdK 33
* `[UPDATED]`: kotlinx.serialization 1.4.0
* `[UPDATED]`: graphql-java 19.2
* `[ADDED]`: iosSimulatorArm64 support

Version 0.10.１ *(2022/05/16)*
---

* `[FIXED]`: revert graphql-java version to 17.3, to fix incompatibility with Android Gradle Plugin

Version 0.10.0 *(2022/05/16)*
---

* `[UPDATED]`: Kotlin 1.6.21
* `[UPDATED]`: Gradle wrapper 7.4.2
* `[UPDATED]`: Android Gradle Plugin 7.1.3
* `[UPDATED]`: Android compileSdk 31
* `[UPDATED]`: graphql-java 18.1

Version 0.9.0 *(2022/02/02)*
---

* `[UPDATED]`: Kotlin 1.6.10
* `[UPDATED]`: GradLe wrapper 7.2
* `[UPDATED]`: kotlinx.serialization 1.3.2
* `[ADDED]`: HMPP support

Version 0.8.2 *(2021/09/10)*
---

* `[FIXED]`: Set source compatibility to Java 8

Version 0.8.1 *(2021/09/10)*
---

* `[FIXED]`: Duplicate content roots warning

Version 0.8.0 *(2021/09/09)*
---

* `[ADDED]`: expose `operationName` from Query object
* `[UPDATED]`: propagate task dependency by source set dependency

Version 0.7.0 *(2021/08/05)*
---

* `[UPDATED]`: Kotlin 1.5.21
* `[UPDATED]`: Android Gradle Plugin 4.2.2
* `[UPDATED]`: kotlinx.serialization 1.2.2
* `[UPDATED]`: gradle wrapper 7.0.2
* `[BREAKING]`: `requestBody` function now returns Request instance. You should encode it on your own

Version 0.6.0 *(2021-06-11)*
---

* `[UPDATED]`: Kotlin 1.5.10
* `[UPDATED]`: kotlinx.serialization 1.2.1
* `[UPDATED]`: gradle wrapper 6.9

Version 0.5.6 *(2021-05-06)*
---

* `[UPDATED]`: Kotlin 1.4.32
* `[UPDATED]`: kotlinx.serialization 1.1.0
* `[UPDATED]`: gradle wrapper 7.0

Version 0.5.5 *(2020-12-01)*
---

* `[UPDATED]`: Kotlin 1.4.20
* `[UPDATED]`: kotlinx.serialization 1.0.1
* `[UPDATED]`: gradle wrapper 6.7.1
* `[ADDED]`: enabled explicit api

Version 0.5.4 *(2020-10-01)*
---

* `[UPDATED]`: rewrite plugin
* `[UPDATED]`: Kotlin 1.4.10
* `[UPDATED]`: kotlinx.serialization 1.0.0-RC2

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
* `[ADDED]`: `requestBody` method now optionally take `kotlinx.serialization.json.Json` instance to customize
  serialization behavior

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

* `[BREAKING]`: Generated Document Objects are now `internal` by
  default ([#13](https://github.com/yshrsmz/kgql/issues/13))
* `[FIXED]`: Fix generated file's output directory not correct.
* `[UPDATED]`: Kotlin 1.3.20 ([#14](https://github.com/yshrsmz/kgql/issues/14))
* `[UPDATED]`: Gradle 5.1.1 ([#14](https://github.com/yshrsmz/kgql/issues/14)) and __5.1.x or later__ is required.
* `[UPDATED]`: Android Gradle Plugin 3.3.0

Version 0.0.7 *(2019-01-22)*
---

* `[ADDED]`: Use `kotlinx.serialization.SerialName` annotation ([#11](https://github.com/yshrsmz/kgql/issues/11))
* `[FIXED]`: Android compilation task now depends on `generateKgqlInterface` task properly in Kotlin multiplatform
  project ([#12](https://github.com/yshrsmz/kgql/issues/12))
* `[FIXED]`: Gradle Plugin now depends on antlr4 to avoid dependency conflict with Android DataBinding

Version 0.0.6 *(2019-01-21)*
---

* `[BREAKING]`: Create dedicated object for each operation in a
  document ([#3](https://github.com/yshrsmz/kgql/issues/3))
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
