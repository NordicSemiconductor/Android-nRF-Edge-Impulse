package no.nordicsemi.android.ei.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Plant a Timber DebugTree to collect logs from sample app and McuManager
        Timber.plant(Timber.DebugTree())
    }
}