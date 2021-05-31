package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.service.EiService

@Module
@InstallIn(ProjectComponent::class)
object RepositoriesModule {

    @Provides
    @ProjectScope
    fun provideProjectsRepository(
        eiService: EiService,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): ProjectRepository =
        ProjectRepository(service = eiService, defaultDispatcher = defaultDispatcher)
}