package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import no.nordicsemi.android.ei.repository.LoginRepository
import no.nordicsemi.android.ei.repository.ProjectsRepository
import no.nordicsemi.android.ei.service.EiService

@Module
@InstallIn(ViewModelComponent::class)
object RepositoriesModule {
    @Provides
    @ViewModelScoped
    //TODO Remove temporary hard coded token
    fun provideToken(): String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ" +
            "1c2VySWQiOjE1NTMxLCJpYXQiOjE2MTU1NTIzNjUsImV4cCI6MTYxODE0NDM2NX0.dNTKCwIZLPiZ7MwsBWxy3n7Doq_s3Jf3PjCsHejo-ug"

    @Provides
    @ViewModelScoped
    fun provideLoginRepository(eiService: EiService): LoginRepository = LoginRepository(eiService)

    @Provides
    @ViewModelScoped
    fun provideProjectsRepository(eiService: EiService): ProjectsRepository =
        ProjectsRepository(eiService)
}