package com.codingfeline.kgql.core.internal

fun <T> isEnumSubClass(value: T?): Boolean = value is Enum<*>
