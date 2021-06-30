package no.nordicsemi.android.ei.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.ei.BuildConfig
import no.nordicsemi.android.ei.model.DeviceMessage
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.util.DeviceMessageTypeAdapter
import no.nordicsemi.android.ei.util.MessageTypeAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceApiModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = when {
                BuildConfig.DEBUG -> Level.BODY
                else -> Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .followRedirects(false)
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideService(client: OkHttpClient): EiService {
        return Retrofit.Builder()
            .baseUrl("https://studio.edgeimpulse.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create()
    }

    @Provides
    @Singleton
    fun provideWebSocketRequest(): Request = Request.Builder()
        .url("wss://remote-mgmt.edgeimpulse.com")
        .build()


    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Message::class.java, MessageTypeAdapter())
        .registerTypeAdapter(DeviceMessage::class.java, DeviceMessageTypeAdapter())
        .create()
}