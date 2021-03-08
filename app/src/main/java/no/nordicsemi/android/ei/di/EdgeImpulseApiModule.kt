package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.ei.eiservice.EiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class EdgeImpulseApiModule {

    @Singleton
    @Provides
    fun provideRepository(): EiService = Retrofit.Builder()
        .baseUrl("https://studio.edgeimpulse.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create()
}