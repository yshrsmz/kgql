package com.codingfeline.kgql.gradle

import org.gradle.api.file.FileCollection

open class KgqlExtension {
    var packageName: String? = null
    var sourceSet: FileCollection? = null
    var typeMapper: Map<String, String>? = null
}
