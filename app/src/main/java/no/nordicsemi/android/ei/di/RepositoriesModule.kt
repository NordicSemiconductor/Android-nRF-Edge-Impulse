package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.service.EiService

@Module
@InstallIn(ProjectComponent::class)
object RepositoriesModule {

    @Provides
    @ProjectScope
    fun provideProjectsRepository(eiService: EiService): ProjectRepository =
        ProjectRepository(eiService)

}