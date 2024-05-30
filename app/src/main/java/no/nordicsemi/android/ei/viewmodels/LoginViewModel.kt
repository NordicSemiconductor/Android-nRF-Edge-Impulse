/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.repository.LoginRepository
import no.nordicsemi.android.ei.viewmodels.state.LoginState
import no.nordicsemi.android.ei.viewmodels.state.LoginState.AwaitingMultiFactorAuthentication
import no.nordicsemi.android.ei.viewmodels.state.LoginState.Error
import no.nordicsemi.android.ei.viewmodels.state.LoginState.InProgress
import no.nordicsemi.android.ei.viewmodels.state.LoginState.LoggedIn
import no.nordicsemi.android.ei.viewmodels.state.LoginState.LoggedOut
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repo: LoginRepository
) : AndroidViewModel(context as Application) {

    var state: LoginState by mutableStateOf(LoggedOut)
        private set

    fun login(username: String, password: String, code: String? = null, authTokenType: String) {
        val context = getApplication() as Context
        state = InProgress
        val handler = CoroutineExceptionHandler { _, throwable ->
            state = Error(throwable)
        }
        viewModelScope.launch(handler) {
            repo.login(username, password, code).let { response ->
                if (response.success) {
                    response.token?.let { token ->
                        state = LoggedIn(username, password, token, authTokenType)
                    } ?: run {
                        val message =
                            response.error ?: context.getString(R.string.error_invalid_token)
                        state = Error(Throwable(message))
                    }
                } else {
                    response.error?.takeIf {
                        // Error thrown by edge impulse when two factor authentication is enabled.
                        // https://docs.edgeimpulse.com/reference/edge-impulse-api/login/get_jwt_token
                        it.contains("ERR_TOTP_TOKEN IS REQUIRED")
                    }?.let {
                        state = AwaitingMultiFactorAuthentication
                    } ?: run {
                        val message =
                            response.error ?: context.getString(R.string.error_unknown)
                        state = Error(Throwable(message))
                    }
                }
            }
        }
    }

    fun cancelLogin() {
        val context = getApplication() as Context
        state = Error(Throwable(context.getString(R.string.error_login_cancelled)))
    }
}
