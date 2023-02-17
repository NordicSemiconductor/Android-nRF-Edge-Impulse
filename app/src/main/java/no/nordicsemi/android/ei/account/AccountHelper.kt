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
import android.accounts.OperationCanceledException
import android.app.Activity
import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ei.R
import java.security.InvalidParameterException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object AccountHelper {

    suspend fun getOrCreateAccount(activity: Activity): Result<Account> {
        val accountManager = AccountManager.get(activity)
        val accountType = activity.getString(R.string.account_type)
        val accountTokenType = activity.getString(R.string.account_token_type)

        try {
            return suspendCancellableCoroutine { continuation ->
                val accounts = accountManager.getAccountsByType(accountType)
                if (accounts.isNotEmpty()) {
                    continuation.resume(Result.success(accounts.first()))
                } else {
                    accountManager.addAccount(
                        accountType, accountTokenType,
                        null, null,
                        activity, { future ->
                            try {
                                future.result // this can throw exceptions
                                val updatedAccounts =
                                    accountManager.getAccountsByType(accountType)
                                continuation.resume(Result.success(updatedAccounts.first()))
                            } catch (e: OperationCanceledException) {
                                continuation.cancel(e)
                            } catch (e: Exception) {
                                continuation.resumeWithException(e)
                            }
                        },
                        null
                    )
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getAuthToken(account: Account, activity: Activity): Result<String> {
        val accountManager = AccountManager.get(activity)
        val accountTokenType = activity.getString(R.string.account_token_type)

        try {
            return suspendCancellableCoroutine { continuation ->
                accountManager.getAuthToken(
                    account, accountTokenType,
                    null,
                    activity, { future ->
                        try {
                            val result = future.result
                            result.getString(AccountManager.KEY_AUTHTOKEN)?.let { token ->
                                continuation.resume(Result.success(token))
                            } ?: run {
                                continuation.resumeWithException(InvalidParameterException("Auth token missing"))
                            }
                        } catch (e: OperationCanceledException) {
                            continuation.cancel(e)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }, null
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun invalidateAuthToken(token: String, context: Context) {
        val accountManager = AccountManager.get(context)
        val accountType = context.getString(R.string.account_type)
        accountManager.invalidateAuthToken(accountType, token)
    }

}