package com.codingfeline.kgql.dsl

class SelectionSet(builder: SelectionSet.() -> Unit) {
    val fields: MutableSet<Any> = mutableSetOf()

    fun field(name: String) {
        fields.add(name)
    }
}
