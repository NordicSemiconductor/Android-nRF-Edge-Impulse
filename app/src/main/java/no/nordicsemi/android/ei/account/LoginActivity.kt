package no.nordicsemi.android.ei.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.Login
import no.nordicsemi.android.ei.ui.theme.NordicTheme
import no.nordicsemi.android.ei.viewmodels.LoginViewModel

@AndroidEntryPoint
class LoginActivity : AccountAuthenticatorActivity() {

    companion object {
        const val KEY_ACCOUNT_NAME = "KEY_ACCOUNT_NAME"
        const val KEY_ACCOUNT_TYPE = "KEY_ACCOUNT_TYPE"
        const val KEY_AUTH_TOKEN_TYPE = "KEY_AUTH_TOKEN_TYPE"
        const val KEY_NEW_ACCOUNT = "KEY_NEW_ACCOUNT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accountType = intent.getStringExtra(KEY_ACCOUNT_TYPE) ?: getString(R.string.account_type)
        val accountName = intent.getStringExtra(KEY_ACCOUNT_NAME)
        val authTokenType = intent.getStringExtra(KEY_AUTH_TOKEN_TYPE) ?: "limited_access"

        val viewModel: LoginViewModel by viewModels()
        viewModel.ready.observe(this) { authData ->
            finishLogin(
                authData.username,
                authData.password,
                accountType,
                authData.tokenType,
                authData.token
            )
        }

        setContent {
            NordicTheme(darkTheme = false) {
                Scaffold(
                    backgroundColor = MaterialTheme.colors.background
                ) { innerPadding ->
                    val busy by viewModel.isInProgress.observeAsState(false)
                    val error by viewModel.error.observeAsState()

                    Login(
                        modifier = Modifier.padding(innerPadding),
                        enabled = !busy,
                        onLogin = { username, password ->
                            viewModel.login(username, password, authTokenType)
                        },
                        onForgotPassword = {
                            open(Uri.parse("https://studio.edgeimpulse.com/forgot-password"))
                        },
                        onSignUp = {
                            open(Uri.parse("https://studio.edgeimpulse.com/signup"))
                        },
                        login = accountName ?: "",
                        error = error
                    )
                }
            }
        }
    }

    /**
     * Opens the given URI in a browser.
     * @param uri The URI to be opened.
     */
    private fun open(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun finishLogin(
        accountName: String,
        accountPassword: String,
        accountType: String,
        authTokenType: String,
        authToken: String
    ) {
        val accountManager = AccountManager.get(this)
        val account = Account(accountName, accountType)
        if (intent.getBooleanExtra(KEY_NEW_ACCOUNT, false)) {
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            accountManager.addAccountExplicitly(account, accountPassword, null)
            accountManager.setAuthToken(account, authTokenType, authToken)
        } else {
            accountManager.setPassword(account, accountPassword)
        }

        val bundle = Bundle()
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName)
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        setAccountAuthenticatorResult(bundle)

        setResult(RESULT_OK, intent)
        finish()
    }

}