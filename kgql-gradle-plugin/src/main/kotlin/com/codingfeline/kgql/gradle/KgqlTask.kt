package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.VERSION
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import java.io.File

class KgqlTask : SourceTask() {
    @Input
    fun pluginVersion() = VERSION

    @get:OutputDirectory
    var outputDirectory: File? = null

    lateinit var sourceFolders: Iterable<File>
    lateinit var packageName: String
}
