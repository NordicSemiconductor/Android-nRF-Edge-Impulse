package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.service.EiService

abstract class BaseRepository(
    protected val service: EiService,
    @DefaultDispatcher protected val defaultDispatcher: CoroutineDispatcher
) {

    suspend fun getCurrentUser(token: String) = withContext(defaultDispatcher) {
        service.getCurrentUser(jwt = token)
    }
}