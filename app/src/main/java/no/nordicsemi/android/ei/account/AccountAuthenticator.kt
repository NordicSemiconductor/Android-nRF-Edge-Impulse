/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.ei.util.guard
import javax.inject.Inject

class AccountAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
) : AbstractAccountAuthenticator(context) {

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val accountManager = AccountManager.get(context)
        val accountExists = accountManager.getAccountsByType(accountType).isNotEmpty()
        guard(!accountExists) {
            val bundle = Bundle()
            bundle.putInt(AccountManager.KEY_ERROR_CODE, 1)
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "Only one account is allowed")
            return bundle
        }

        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtra(LoginActivity.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(LoginActivity.KEY_AUTH_TOKEN_TYPE, authTokenType)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)

        return bundle
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle = AccountManager.get(context).peekAuthToken(account!!, authTokenType)?.let { authToken ->
        val result = Bundle()
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
        result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        result
    } ?: run {
        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtra(LoginActivity.KEY_ACCOUNT_NAME, account.name)
        intent.putExtra(LoginActivity.KEY_ACCOUNT_TYPE, account.type)
        intent.putExtra(LoginActivity.KEY_AUTH_TOKEN_TYPE, authTokenType)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        bundle
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        TODO("Not yet implemented")
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle {
        TODO("Not yet implemented")
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        TODO("Not yet implemented")
    }

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle {
        TODO("Not yet implemented")
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        TODO("Not yet implemented")
    }
}