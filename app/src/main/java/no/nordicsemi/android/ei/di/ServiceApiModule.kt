package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import no.nordicsemi.android.ei.service.EiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

@Module
@InstallIn(ViewModelComponent::class)
object ServiceApiModule {

    @Provides
    fun provideRepository(): EiService = Retrofit.Builder()
        .baseUrl("https://studio.edgeimpulse.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create()

}