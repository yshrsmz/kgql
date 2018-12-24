package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.VersionKt
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class KgqlTask2 extends SourceTask {
    @Input
    String pluginVersion() {
        return VersionKt.getVERSION()
    }

    @OutputDirectory
    File outputDirectory = null

    Iterable<File> sourceFolders
    String packageName
    Map<String, String> typeMap

    @TaskAction
    void generateKgqlFiles() {

    }
}
