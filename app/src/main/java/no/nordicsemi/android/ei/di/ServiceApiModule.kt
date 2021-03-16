package no.nordicsemi.android.ei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.ei.service.EiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceApiModule {

    @Provides
    @Singleton
    fun provideService(): EiService {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .followRedirects(false)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://studio.edgeimpulse.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create()
    }

}