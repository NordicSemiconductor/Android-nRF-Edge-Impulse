package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.service.EiService

abstract class BaseRepository(
    protected val service: EiService
) {
    suspend fun getCurrentUser(token: String) = withContext(Dispatchers.IO) {
        service.getCurrentUser(jwt = token)
    }
}