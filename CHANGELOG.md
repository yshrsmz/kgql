Change Log
===

Version 0.0.8 *(WIP)*
---

* Generated Document Objects are now `internal` by default [#13](https://github.com/yshrsmz/kgql/issues/13)
* Kotlin is updated to 1.3.20 [#14](https://github.com/yshrsmz/kgql/issues/14)
* Gradle is updated to 4.10.2 [#14](https://github.com/yshrsmz/kgql/issues/14)


Version 0.0.7 *(2019-01-22)*
---

* Use `kotlinx.serialization.SerialName` annotation [#11](https://github.com/yshrsmz/kgql/issues/11)
* Android compilation task now depends on `generateKgqlInterface` task properly in Kotlin multiplatform project [#12](https://github.com/yshrsmz/kgql/issues/12)
* Gradle Plugin now depends on antlr4 to avoid dependency conflict with Android DataBinding


Version 0.0.6 *(2019-01-21)*
---

* Create dedicated object for each operation in a document([#3](https://github.com/yshrsmz/kgql/issues/3))
* Change KgqlRequestBody & KgqlResponse to interface([#8](https://github.com/yshrsmz/kgql/issues/8))
* Change suffix of generate classes to Document, from DocumentWrapper
* Downgrade to Kotlin 1.3.11 [#8](https://github.com/yshrsmz/kgql/issues/8)


Version 0.0.5 *(2019-01-15)*
---

* Release via `gradle-mvn-mpp-push.gradle`


Version 0.0.4 *(2019-01-14)*
---

* Fix dependency resolution



Version 0.0.3 *(2019-01-14)*
---

* Initial preview release
