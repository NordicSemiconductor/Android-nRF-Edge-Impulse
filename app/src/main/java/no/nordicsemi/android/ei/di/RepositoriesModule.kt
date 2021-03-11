package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.repository.LoginRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {

    @Provides
    @ViewModelScoped
    fun provideRepository(eiService: EiService): LoginRepository {
        return LoginRepository(eiService)
    }

}