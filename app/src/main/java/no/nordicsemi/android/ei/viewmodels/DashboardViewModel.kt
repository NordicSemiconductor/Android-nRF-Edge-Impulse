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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.repository.DashboardRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val dashboardRepository: DashboardRepository,
) : AndroidViewModel(context as Application) {

    // User is kept outside of refresh state, as it is available also when refreshing.
    var user: User by mutableStateOf(repo.user)
        private set

    var isRefreshing: Boolean by mutableStateOf(false)
        private set

    private var _error = MutableSharedFlow<Throwable>()
    val error: SharedFlow<Throwable> = _error.asSharedFlow()

    private val repo: UserDataRepository
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository()

    fun refreshUser() {
        isRefreshing = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { _error.emit(throwable) }
                .also { isRefreshing = false }
        }
        viewModelScope.launch(handler) {
            dashboardRepository
                .getCurrentUser(repo.token)
                .apply { userManager.userLoggedIn(this, repo.token) }
                .apply { user = this }
                .also { isRefreshing = false }
        }
    }

    fun createProject(projectName:String) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch { _error.emit(throwable) }
        }
        viewModelScope.launch(handler) {
            dashboardRepository.createProject(repo.token, projectName)
        }
    }

    fun logout() {
        AccountHelper.invalidateAuthToken(repo.token, getApplication())
        userManager.logout()
    }
}