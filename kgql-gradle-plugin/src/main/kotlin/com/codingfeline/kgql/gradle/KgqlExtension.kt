package com.codingfeline.kgql.gradle

import org.gradle.api.file.FileCollection
import java.io.File

open class KgqlExtension {
    var packageName: String? = null
    var sourceSet: FileCollection? = null
    var typeMapper: MutableMap<String, String>? = null
    var schemaJson: File? = null
}
