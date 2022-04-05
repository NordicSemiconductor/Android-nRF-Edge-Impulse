package no.nordicsemi.android.ei.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.repository.LoginRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userManager: UserManager,
    private val loginRepository: LoginRepository
): ViewModel() {

    suspend fun getUserData(token: String) =
        loginRepository
            .getCurrentUser(token)
            .let { user -> userManager.userLoggedIn(user, token) }

}