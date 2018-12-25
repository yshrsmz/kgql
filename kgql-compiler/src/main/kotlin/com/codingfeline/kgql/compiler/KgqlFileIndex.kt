package com.codingfeline.kgql.compiler

import java.io.File

interface KgqlFileIndex {
    /**
     * @return true if this index is configured to be used by Kgql
     */
    val isConfigured: Boolean

    /**
     * @return the path to the output directory generated code should be in, relative to [contentRoot]
     */
    val outputDirectory: String

    /**
     * @return the package name for the whole source set. This is equivalent to the package name
     * found in the manifest file for the current variant
     */
    val packageName: String

    /**
     * @return the content root for the [Module] backing this index.
     */
    val contentRoot: String

    /**
     * @return the package name for a given Kgql file. Equal to the relative path under its fixture's kgql directory.
     */
    fun packageName(file: File): String

    /**
     * @return the source roots of kgql files for [file].
     */
    fun sourceFolders(file: File): Collection<File>
}
