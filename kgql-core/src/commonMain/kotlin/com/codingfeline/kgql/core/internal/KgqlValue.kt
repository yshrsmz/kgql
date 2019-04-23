package com.codingfeline.kgql.core.internal

/**
 * Class to express GraphQL's explicit/implicit null
 */
sealed class KgqlValue<out T : Any?> {
    data class Some<out T : Any?>(val value: T) : KgqlValue<T>()
    object None : KgqlValue<Nothing>()
}
