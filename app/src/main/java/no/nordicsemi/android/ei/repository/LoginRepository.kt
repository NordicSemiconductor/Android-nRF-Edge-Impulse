package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.LoginRequest
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val service: EiService
) {

    suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        service.login(loginRequest = LoginRequest(username, password, null))
    }

    suspend fun getCurrentUser(token: String) = withContext(Dispatchers.IO) {
        service.getCurrentUser(jwt = token)
    }

}