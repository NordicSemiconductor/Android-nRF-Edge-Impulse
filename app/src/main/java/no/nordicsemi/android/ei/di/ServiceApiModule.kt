package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import no.nordicsemi.android.ei.service.EiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import okhttp3.OkHttpClient

import okhttp3.logging.HttpLoggingInterceptor




@Module
@InstallIn(ViewModelComponent::class)
object ServiceApiModule {

    @Provides
    fun provideRepository(): EiService {
        val interceptor = HttpLoggingInterceptor()
        interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl("https://studio.edgeimpulse.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create()
    }

}