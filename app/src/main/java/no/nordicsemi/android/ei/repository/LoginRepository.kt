package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.LoginRequest
import javax.inject.Inject

class LoginRepository @Inject constructor(
    service: EiService,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : BaseRepository(service = service, defaultDispatcher = defaultDispatcher) {

    suspend fun login(username: String, password: String) = withContext(defaultDispatcher) {
        service.login(loginRequest = LoginRequest(username, password, null))
    }
}