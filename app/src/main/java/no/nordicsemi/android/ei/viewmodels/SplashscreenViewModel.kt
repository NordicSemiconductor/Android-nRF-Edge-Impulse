package no.nordicsemi.android.ei.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.repository.LoginRepository
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SplashscreenViewModel @Inject constructor(
    private val userManager: UserManager,
    private val loginRepository: LoginRepository
): ViewModel() {

    @Throws(HttpException::class)
    suspend fun getUserData(token: String) =
        loginRepository.getCurrentUser(token).apply {
            takeIf { it.isSuccessful }?.body()?.let { user ->
                userManager.userLoggedIn(user, token)
            } ?: run {
                throw HttpException(this)
            }
        }

}