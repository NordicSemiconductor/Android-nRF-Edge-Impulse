/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.ui

import android.accounts.OperationCanceledException
import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.ui.theme.NordicSun
import no.nordicsemi.android.ei.viewmodels.UserViewModel
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Composable
fun Login(
    viewModel: UserViewModel = viewModel(),
    onLoggedIn: () -> Unit = {},
    onCancelled: () -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    val activity = LocalContext.current as Activity
    var state by rememberLoadingState(LoadingState.LoggingIn)
    var retry by remember { mutableIntStateOf(0) }

    when (state) {
        is LoadingState.Error ->
            LoadingFailed(
                message = stringResource(id = state.messageResId),
                onTryAgain = { retry += 1 }
            )
        is LoadingState.LoggingIn,
        is LoadingState.ObtainingUserData ->
            LoadingProgressIndicator(message = stringResource(id = state.messageResId))

    }
    LaunchedEffect(retry) {
        val account = AccountHelper.getOrCreateAccount(activity).getOrElse { reason ->
            when (reason) {
                is OperationCanceledException -> onCancelled()
                else -> onError(reason)
            }
            return@LaunchedEffect
        }
        while (true) {
            state = LoadingState.LoggingIn
            val token = AccountHelper.getAuthToken(account, activity).getOrElse { reason ->
                when (reason) {
                    is OperationCanceledException -> onCancelled()
                    else -> onError(reason)
                }
                return@LaunchedEffect
            }
            state = LoadingState.ObtainingUserData
            try {
                viewModel.getUserData(token)
                onLoggedIn()
            } catch (e: UnknownHostException) {
                // No Internet
                state = LoadingState.Error(e)
            } catch (e: SocketTimeoutException) {
                // Timeout
                state = LoadingState.Error(e)
            } catch (e: HttpException) {
                if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    AccountHelper.invalidateAuthToken(token, activity)
                    continue
                }
                state = LoadingState.Error(e)
            }
            break
        }
    }
}

@Composable
fun LoadingProgressIndicator(
    modifier: Modifier = Modifier,
    message: String = "",
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(180.dp),
            /*color = if (MaterialTheme.colorScheme.isLight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryVariant,*/
            strokeWidth = 12.dp
        )
        Text(
            text = message,
            modifier = Modifier.offset(y = 120.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LoadingFailed(
    message: String,
    modifier: Modifier = Modifier,
    onTryAgain: () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Icon(
            Icons.Rounded.Warning,
            modifier = Modifier.size(180.dp),
            contentDescription = "",
            tint = NordicSun
        )
        Text(
            text = message,
            modifier = Modifier.offset(y = 90.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(
            onClick = onTryAgain,
            modifier = Modifier.offset(y = 140.dp)
        ) {
            Text(text = stringResource(id = R.string.action_try_again))
        }
    }
}

sealed class LoadingState(@StringRes open val messageResId: Int) {
    data object LoggingIn : LoadingState(R.string.label_logging_in)
    data object ObtainingUserData : LoadingState(R.string.label_obtaining_user_data)
    data class Error(@StringRes override val messageResId: Int) : LoadingState(messageResId) {
        constructor(throwable: Throwable) : this(
            when (throwable) {
                is UnknownHostException -> R.string.error_no_internet
                is SocketTimeoutException -> R.string.error_timeout
                else -> R.string.error_obtaining_user_data_failed
            }
        )
    }

    companion object {
        fun Saver() =
            Saver<LoadingState, Int>(
                save = { it.messageResId },
                restore = { messageResId ->
                    when (messageResId) {
                        R.string.label_logging_in -> LoggingIn
                        R.string.label_obtaining_user_data -> ObtainingUserData
                        else -> Error(messageResId)
                    }
                }
            )
    }
}


@Composable
fun rememberLoadingState(
    initialValue: LoadingState = LoadingState.LoggingIn
): MutableState<LoadingState> {
    return rememberSaveable(stateSaver = LoadingState.Saver()) {
        mutableStateOf(initialValue)
    }
}
