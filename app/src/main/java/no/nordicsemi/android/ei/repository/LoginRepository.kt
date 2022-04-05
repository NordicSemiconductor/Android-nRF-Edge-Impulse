package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.IODispatcher
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.LoginRequest
import javax.inject.Inject

class LoginRepository @Inject constructor(
    service: EiService,
    @IODispatcher isDispatcher: CoroutineDispatcher
) : BaseRepository(service = service, ioDispatcher = isDispatcher) {

    suspend fun login(username: String, password: String) = withContext(ioDispatcher) {
        service.login(loginRequest = LoginRequest(username, password, null))
    }
}