package no.nordicsemi.android.ei.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.repository.LoginRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userManager: UserManager,
    private val loginRepository: LoginRepository,
) : ViewModel() {

    private val _pullToRefresh = MutableLiveData(false)
    val pullToRefresh: LiveData<Boolean> = _pullToRefresh

    private val repo: UserDataRepository
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository()

    private val _user = MutableLiveData(repo.user)
    val user: LiveData<User> = _user

    fun refreshUser() {
        _pullToRefresh.value = true
        viewModelScope.launch {
            loginRepository.getCurrentUser(repo.token).apply {
                _pullToRefresh.value = false
                takeIf { it.isSuccessful }?.body()?.let { user ->
                    userManager.userLoggedIn(user, repo.token)
                    _user.value = user
                } ?: run {
                    //TODO handle internet connectivity issues
                }
            }
        }
    }

    fun logOut(){
        userManager.logout()
    }
}