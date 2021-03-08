package no.nordicsemi.android.ei.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import no.nordicsemi.android.ei.eiservice.EiService
import no.nordicsemi.android.ei.repository.NrfEiRepository

@InstallIn(ViewModelComponent::class)
@Module
object NrfEiRepositoryModule {

    @Provides
    fun provideRepository(context: Context, eiService: EiService): NrfEiRepository {
        return NrfEiRepository(context, eiService)
    }
}