package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.repository.LoginRepository
import java.net.UnknownHostException
import javax.inject.Inject

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

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repo: LoginRepository
) : AndroidViewModel(context as Application) {

    var state: LoginState by mutableStateOf(LoginState.LoggedOut)

    fun login(username: String, password: String, authTokenType: String) {
        val context = getApplication() as Context
        state = LoginState.InProgress
        val handler = CoroutineExceptionHandler { _, throwable ->
            val message = when (throwable) {
                is UnknownHostException -> context.getString(R.string.error_no_internet)
                else -> throwable.localizedMessage
            }
            state = LoginState.Error(message)
        }
        viewModelScope.launch(handler) {
            repo.login(username, password).let { response ->
                response.token?.let { token ->
                    state = LoginState.LoggedIn(username, password, token, authTokenType)
                } ?: run {
                    val message = response.error ?: context.getString(R.string.error_invalid_token)
                    state = LoginState.Error(message)
                }
            }
        }
    }

}