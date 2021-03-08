package no.nordicsemi.android.ei.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ContextModule {

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context = context
}