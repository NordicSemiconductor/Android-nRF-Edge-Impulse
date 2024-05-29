/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.viewmodels.state

sealed class LoginState {
    data object LoggedOut : LoginState()
    data object InProgress : LoginState()
    data object AwaitingMultiFactorAuthentication : LoginState()
    data class LoggedIn(
        val username: String,
        val password: String,
        val token: String,
        val tokenType: String
    ) : LoginState()

    data class Error(val error: Throwable) : LoginState()
}