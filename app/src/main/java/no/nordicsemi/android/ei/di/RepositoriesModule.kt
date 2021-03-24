package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.service.EiService

@Module
@InstallIn(ViewModelComponent::class)
object RepositoriesModule {

    @Provides
    @ViewModelScoped
    fun provideProjectsRepository(eiService: EiService): ProjectRepository =
        ProjectRepository(eiService)

}