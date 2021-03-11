package no.nordicsemi.android.ei.viewmodels

import android.util.Log
import androidx.lifecycle.*
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
        val tokenType: String?
    )

    private val _isInProgress = MutableLiveData(false)
    private val _error = MutableLiveData<String?>()
    private val _authData = MutableLiveData<AuthData>()

    val isInProgress: LiveData<Boolean>
        get() = _isInProgress

    val error: LiveData<String?>
        get() = _error

    val ready: LiveData<AuthData>
        get() = _authData

    fun login(username: String, password: String, authTokenType: String?) {
        _isInProgress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            repo.login(username, password).run {
                withContext(Dispatchers.Main) {
                    _isInProgress.value = false
                    if (token != null) _authData.value = AuthData(username, password, token, authTokenType)
                    else _error.value = error ?: "Invalid token"
                }
            }
        }
    }

}