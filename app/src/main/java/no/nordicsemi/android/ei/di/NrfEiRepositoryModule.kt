package no.nordicsemi.android.ei.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.ei.eiservice.EiService
import no.nordicsemi.android.ei.repository.NrfEiRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NrfEiRepositoryModule {

    @Singleton
    @Provides
    fun provideRetrofit(context: Context, eiService: EiService): NrfEiRepository {
        return NrfEiRepository(context, eiService)
    }
}