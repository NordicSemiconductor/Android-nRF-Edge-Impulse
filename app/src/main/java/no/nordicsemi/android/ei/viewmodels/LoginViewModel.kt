package no.nordicsemi.android.ei.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.repository.LoginRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: LoginRepository
) : ViewModel() {

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
        _isInProgress.value = true
        viewModelScope.launch {
            repo.login(username, password).let { response ->
                _isInProgress.value = false
                response.token?.let { token ->
                    _authData.value = AuthData(username, password, token, authTokenType)
                } ?: run {
                    _error.value = response.error ?: "Invalid token"
                }
            }
        }
    }

}