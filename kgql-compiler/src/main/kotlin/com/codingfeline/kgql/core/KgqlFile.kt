package com.codingfeline.kgql.core

import java.io.File

data class KgqlFile(
    val packageName: String,
    val outputDirectory: File,
    val sourceFile: File
)
