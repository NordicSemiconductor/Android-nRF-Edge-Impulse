package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.repository.LoginRepository

@Module
@InstallIn(ViewModelComponent::class)
object RepositoriesModule {

    @Provides
    @ViewModelScoped
    fun provideRepository(eiService: EiService): LoginRepository {
        return LoginRepository(eiService)
    }

}