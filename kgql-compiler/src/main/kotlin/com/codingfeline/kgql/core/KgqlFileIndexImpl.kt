package com.codingfeline.kgql.core

import java.io.File

class KgqlFileIndexImpl : KgqlFileIndex {
    override val isConfigured: Boolean
        get() = false

    override val outputDirectory: String
        get() = throw UnsupportedOperationException()
    override val packageName: String
        get() = throw UnsupportedOperationException()
    override val contentRoot: String
        get() = throw UnsupportedOperationException()

    override fun packageName(file: File): String = throw UnsupportedOperationException()

    override fun sourceFolders(file: File): Collection<File> = listOf(file.parentFile)
}
