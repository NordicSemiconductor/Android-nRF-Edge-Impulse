package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.repository.LoginRepository
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repo: LoginRepository
) : AndroidViewModel(context as Application) {

    data class AuthData(
        val username: String,
        val password: String,
        val token: String,
        val tokenType: String
    )

    var isInProgress: Boolean by mutableStateOf(false)
        private set

    var error: String? by mutableStateOf(null)
        private set

    private val _authData = MutableLiveData<AuthData>()
    val ready: LiveData<AuthData>
        get() = _authData

    fun login(username: String, password: String, authTokenType: String) {
        val context = getApplication() as Context
        isInProgress = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            when (throwable) {
                is UnknownHostException -> error = context.getString(R.string.error_no_internet)
                else -> error = throwable.localizedMessage
            }
            isInProgress = false
        }
        viewModelScope.launch(handler) {
            repo.login(username, password).let { response ->
                isInProgress = false
                response.token?.let { token ->
                    _authData.value = AuthData(username, password, token, authTokenType)
                } ?: run {
                    error = response.error ?: context.getString(R.string.error_invalid_token)
                }
            }
        }
    }

}