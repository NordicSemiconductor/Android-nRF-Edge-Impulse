/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.util

/**
 * Checks if the receiver is `null` and if so, executes the `nullClause`, forcing an early exit.
 * @param nullClause A block to be performed if receiver is null. This block must end with a
 *                   `return` statement, forcing an early exit from surrounding scope on `null`.
 * @return The receiver, now guaranteed not to be null.
 */
inline fun<T> T?.guard(nullClause: () -> Nothing): T {
	return this ?: nullClause()
}

/**
 * Checks if the receiver is `null` and if so, executes the `nullClause`, forcing an early exit.
 * @param condition  The condition to check.
 * @param nullClause A block to be performed if receiver is null. This block must end with a
 *                   `return` statement, forcing an early exit from surrounding scope on `null`.
 * @return The receiver, now guaranteed not to be null.
 */
inline fun<T> T?.guard(condition: T.() -> Boolean, nullClause: () -> Nothing): T {
	return this?.takeIf { condition(it) } ?: nullClause()
}

/**
 * Checks if the condition is true. Otherwise, executes the `elseClause`, forcing an early exit.\
 * @param condition  The condition to check.
 * @param elseClause A block to be performed if condition is false. This block must end with a
 * 					 `return` statement, forcing an early exit from surrounding scope on `false`.
 */
inline fun guard(condition: Boolean, elseClause: () -> Nothing) {
	if (!condition) elseClause()
}