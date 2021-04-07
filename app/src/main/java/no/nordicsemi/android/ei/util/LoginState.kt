package no.nordicsemi.android.ei.util

sealed class LoginState {
    object LoggedOut: LoginState()
    object InProgress: LoginState()
    data class LoggedIn(
        val username: String,
        val password: String,
        val token: String,
        val tokenType: String
    ): LoginState()
    data class Error(val message: String): LoginState()
}