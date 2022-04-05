package no.nordicsemi.android.ei.account

import android.accounts.AbstractAccountAuthenticator
import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticatorService: Service() {
    @Inject lateinit var authenticator: AbstractAccountAuthenticator

    override fun onBind(intent: Intent?): IBinder? = authenticator.iBinder
}