package com.codingfeline.kgql.compiler

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import java.io.File

@JsonClass(generateAdapter = true)
class KgqlPropertiesFile(
    val packageName: String,
    val sourceSets: List<List<String>>,
    val outputDirectory: String
) {

    fun toFile(file: File) {
        file.writeText(adapter.toJson(this))
    }

    companion object {
        const val NAME = ".kgql"

        private val adapter by lazy {
            val moshi = Moshi.Builder().build()
            moshi.adapter(KgqlPropertiesFile::class.java)
        }

        fun fromText(text: String) = adapter.fromJson(text)
        fun fromFile(file: File): KgqlPropertiesFile = fromText(file.readText())!!
    }
}
