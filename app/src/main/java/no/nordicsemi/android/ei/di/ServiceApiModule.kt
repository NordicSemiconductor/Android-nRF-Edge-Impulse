package no.nordicsemi.android.ei.di

import android.accounts.AbstractAccountAuthenticator
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.ei.account.AccountAuthenticator
import no.nordicsemi.android.ei.service.EiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import okhttp3.OkHttpClient

import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceApiModule {

    @Provides
    fun provideAuthenticator(@ApplicationContext context: Context): AbstractAccountAuthenticator {
        return AccountAuthenticator(context)
    }

    @Provides
    @Singleton
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