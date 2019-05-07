package com.codingfeline.kgql.core.internal

/**
 * Class to express GraphQL's explicit/implicit null.
 */
sealed class KgqlValue<out T : Any?> {
    /**
     * Represents value presence
     */
    data class Some<out T : Any?>(
        /**
         * Actual value. can be null.
         * we need the fact that null is intentionally set to distinguish it from implicit null.
         */
        val value: T
    ) : KgqlValue<T>()

    /**
     * represents value is not set(implicitly null)
     */
    object None : KgqlValue<Nothing>()
}
