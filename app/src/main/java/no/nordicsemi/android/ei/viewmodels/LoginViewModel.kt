package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
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

    private val _isInProgress = MutableLiveData(false)
    private val _authData = MutableLiveData<AuthData>()
    private val _error = MutableLiveData<String?>()

    val isInProgress: LiveData<Boolean>
        get() = _isInProgress

    val error: LiveData<String?>
        get() = _error

    val ready: LiveData<AuthData>
        get() = _authData

    fun login(username: String, password: String, authTokenType: String) {
        val context = getApplication() as Context
        _isInProgress.value = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            when (throwable) {
                is UnknownHostException -> _error.value = context.getString(R.string.error_no_internet)
                else -> _error.value = throwable.localizedMessage
            }
            _isInProgress.value = false
        }
        viewModelScope.launch(handler) {
            repo.login(username, password).let { response ->
                _isInProgress.value = false
                response.token?.let { token ->
                    _authData.value = AuthData(username, password, token, authTokenType)
                } ?: run {
                    _error.value = response.error ?: context.getString(R.string.error_invalid_token)
                }
            }
        }
    }

}