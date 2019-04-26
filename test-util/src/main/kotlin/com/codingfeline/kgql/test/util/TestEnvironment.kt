package com.codingfeline.kgql.test.util

import com.codingfeline.kgql.compiler.GraphQLCustomTypeFQName
import com.codingfeline.kgql.compiler.GraphQLCustomTypeName
import com.codingfeline.kgql.compiler.KgqlEnvironment
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.stream.Collectors

internal class TestEnvironment(private val outputDirectory: File = File("output")) {

    fun build(
        root: String,
        typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName> = emptyMap(),
        enumNameSet: Set<String> = emptySet()
    ): KgqlEnvironment {
        val files =
            Files.find(
                File(root).toPath(),
                Int.MAX_VALUE,
                BiPredicate { t: Path, u: BasicFileAttributes -> t.toString().endsWith(".gql") })
                .map { it.toFile() }
                .collect(Collectors.toList())
        return KgqlEnvironment(files, "com.example", outputDirectory, typeMap, enumNameSet)
    }
}
