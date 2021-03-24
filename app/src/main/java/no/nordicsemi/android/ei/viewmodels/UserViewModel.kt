package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.repository.LoginRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val loginRepository: LoginRepository,
) : AndroidViewModel(context as Application) {

    var pullToRefresh: Boolean by mutableStateOf(false)
        private set

    var error: Throwable? by mutableStateOf(null)
        private set

    var user: User by mutableStateOf(repo.user)
        private set

    private val repo: UserDataRepository
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository()

    fun refreshUser() {
        pullToRefresh = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            error = throwable
            pullToRefresh = false
        }
        viewModelScope.launch(handler) {
            loginRepository
                .getCurrentUser(repo.token)
                .apply { userManager.userLoggedIn(this, repo.token) }
                .apply { user = this }
                .also { pullToRefresh = false }
        }
    }

    fun logout(){
        AccountHelper.invalidateAuthToken(repo.token, getApplication())
        userManager.logout()
    }
}