/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.account

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * This is [android.accounts.AccountAuthenticatorActivity] converted to [AppCompatActivity].
 * @see <a href="https://developer.android.com/reference/android/accounts/AccountAuthenticatorActivity">AccountAuthenticatorActivity</a>
 */
open class AccountAuthenticatorActivity: AppCompatActivity() {
    private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var resultBundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountAuthenticatorResponse =
            intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        accountAuthenticatorResponse?.onRequestContinued()
    }

    override fun finish() {
        accountAuthenticatorResponse?.let { authenticator ->
            resultBundle?.let { result ->
                authenticator.onResult(result)
            } ?: run {
                authenticator.onError(
                    AccountManager.ERROR_CODE_CANCELED, "cancelled"
                )
            }
        }
        super.finish()
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the
     *               [android.accounts.AbstractAccountAuthenticator] request.
     */
    @Suppress("unused")
    fun setAccountAuthenticatorResult(result: Bundle) {
        resultBundle = result
    }
}