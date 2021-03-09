package no.nordicsemi.android.ei.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.repository.LoginRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: LoginRepository
) : ViewModel() {

    fun login(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repo.login(username, password)
            Log.e("AA", response.toString())
            val projects = repo.projects(response.token)
            Log.e("AA", projects.toString())
        }
    }

}