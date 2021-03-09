package no.nordicsemi.android.ei.repository

import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.LoginRequest

class LoginRepository(private val service: EiService) {

    suspend fun login(username: String, password: String) =
        service.login(loginRequest = LoginRequest(username, password))

    suspend fun projects(token: String) =
        service.projects(token)

}