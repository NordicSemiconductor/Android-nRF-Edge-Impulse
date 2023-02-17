/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

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
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.Uris
import no.nordicsemi.android.ei.ui.Login
import no.nordicsemi.android.ei.ui.theme.NordicTheme
import no.nordicsemi.android.ei.util.asMessage
import no.nordicsemi.android.ei.viewmodels.LoginViewModel
import no.nordicsemi.android.ei.viewmodels.state.LoginState

@AndroidEntryPoint
class LoginActivity : AccountAuthenticatorActivity() {

    companion object {
        const val KEY_ACCOUNT_NAME = "KEY_ACCOUNT_NAME"
        const val KEY_ACCOUNT_TYPE = "KEY_ACCOUNT_TYPE"
        const val KEY_AUTH_TOKEN_TYPE = "KEY_AUTH_TOKEN_TYPE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accountType = intent.getStringExtra(KEY_ACCOUNT_TYPE) ?: getString(R.string.account_type)
        val accountName = intent.getStringExtra(KEY_ACCOUNT_NAME)
        val authTokenType = intent.getStringExtra(KEY_AUTH_TOKEN_TYPE) ?: getString(R.string.account_token_type)

        setContent {
            NordicTheme {
                Scaffold(
                    backgroundColor = MaterialTheme.colors.background
                ) { innerPadding ->
                    val viewModel: LoginViewModel by viewModels()
                    when (val state = viewModel.state) {
                        is LoginState.LoggedIn -> finishLogin(
                            state.username,
                            accountType,
                            state.tokenType,
                            state.token
                        )
                        else -> Login(
                            modifier = Modifier.padding(innerPadding),
                            enabled = state !is LoginState.InProgress,
                            onLogin = { username, password ->
                                viewModel.login(username, password, authTokenType)
                            },
                            onForgotPassword = {
                                open(Uris.ForgetPassword)
                            },
                            onSignUp = {
                                open(Uris.SignUp)
                            },
                            login = accountName ?: "",
                            error = (state as? LoginState.Error)?.error?.asMessage()
                        )
                    }
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
        accountType: String,
        authTokenType: String,
        authToken: String
    ) {
        val accountManager = AccountManager.get(this)

        // The app supports only a single account.
        // If there was one already, rename it to the new account name.
        val accounts = accountManager.getAccountsByType(accountType)
        val account = accounts.firstOrNull() ?: run {
            Account(accountName, accountType).also { account ->
                accountManager.addAccountExplicitly(account, "" /* DON'T STORE PASSWORD */, null)
            }
        }
        accountManager.setAuthToken(account, authTokenType, authToken)

        // If the account name has changed, rename it.
        val oldAccountName = intent.getStringExtra(KEY_ACCOUNT_NAME)
        oldAccountName?.takeIf { accountName != oldAccountName }?.run {
            accountManager.renameAccount(account, accountName, {
                finalize(accountType, accountName, authToken)
            }, null)
        } ?: run {
            finalize(accountType, accountName, authToken)
        }
    }

    private fun finalize(
        accountType: String,
        accountName: String,
        authToken: String
    ) {
        val bundle = Bundle()
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName)
        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        setAccountAuthenticatorResult(bundle)

        setResult(RESULT_OK, intent)
        finish()
    }

}