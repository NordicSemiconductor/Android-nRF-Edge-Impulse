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
import no.nordicsemi.android.ei.viewmodels.state.LoginState
import no.nordicsemi.android.ei.viewmodels.state.LoginState.*
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repo: LoginRepository
) : AndroidViewModel(context as Application) {

    var state: LoginState by mutableStateOf(LoggedOut)
        private set

    fun login(username: String, password: String, authTokenType: String) {
        val context = getApplication() as Context
        state = InProgress
        val handler = CoroutineExceptionHandler { _, throwable ->
            state = Error(throwable)
        }
        viewModelScope.launch(handler) {
            repo.login(username, password).let { response ->
                response.token?.let { token ->
                    state = LoggedIn(username, password, token, authTokenType)
                } ?: run {
                    val message = response.error ?: context.getString(R.string.error_invalid_token)
                    state = Error(Throwable(message))
                }
            }
        }
    }

}