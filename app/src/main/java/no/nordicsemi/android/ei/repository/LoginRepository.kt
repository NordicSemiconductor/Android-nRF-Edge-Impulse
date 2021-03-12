package no.nordicsemi.android.ei.repository

import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.LoginRequest
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val service: EiService
) {

    suspend fun login(username: String, password: String) =
        service.login(loginRequest = LoginRequest(username, password, null))

}