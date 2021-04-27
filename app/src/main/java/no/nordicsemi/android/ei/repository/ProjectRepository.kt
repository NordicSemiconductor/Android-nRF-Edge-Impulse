package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.service.EiService
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val service: EiService,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
}