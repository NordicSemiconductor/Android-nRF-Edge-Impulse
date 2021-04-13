package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.LoginRequest
import javax.inject.Inject

class LoginRepository @Inject constructor(
    service: EiService
) : BaseRepository(service = service) {

    suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        service.login(loginRequest = LoginRequest(username, password, null))
    }
}