package com.codingfeline.kgql.dsl

class Operation(
    val type: OperationType,
    val name: String = "",
    val variables: Set<Pair<String, InputValueType>> = emptySet(),
    val fields: List<SelectionSet> = emptyList()
) {

    fun print(): String {

        val opNameAndVars = if (variables.isNotEmpty()) {
            if (name.isEmpty()) {
                throw IllegalArgumentException("name must not be empty if variables is not empty")
            }
            "$name(${variables.map { "\"${it.first}\": ${it.second}" }.joinToString(",")})"
        } else {
            name
        }
        return """
            |${type.value} $opNameAndVars {
            |}
        """.trimMargin()
    }
}
