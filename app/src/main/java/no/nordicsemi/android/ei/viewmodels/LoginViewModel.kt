package no.nordicsemi.android.ei.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.repository.LoginRepository
import no.nordicsemi.android.ei.service.param.LoginResponse
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: LoginRepository
) : ViewModel() {

    private val _username = MutableLiveData("")
    val username: LiveData<String> = _username

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _loginResponse = MutableLiveData(LoginResponse())
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    fun onUsernameChange(username: String) {
        _username.value = username
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val loginResponse = repo.login(username, password)
            Log.i("AA", loginResponse.toString())
            _loginResponse.value = loginResponse
        }
    }

}