package com.codingfeline.kgql.dsl

import kotlin.test.Test

class Test {

    @Test
    fun test() {
        val q = query("users") {

        }

        println(q.print())
    }
}
