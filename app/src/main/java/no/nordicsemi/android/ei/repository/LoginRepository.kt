package no.nordicsemi.android.ei.repository

import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.`object`.LoginBody

class LoginRepository(private val service: EiService) {

    suspend fun login(username: String, password: String) =
        service.login(loginBody = LoginBody(username, password))

}