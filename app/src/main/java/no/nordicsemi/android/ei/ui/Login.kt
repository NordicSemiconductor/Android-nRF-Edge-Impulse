/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.theme.NordicBlue

@Composable
fun Login(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onLogin: (username: String, password: String) -> Unit = { _: String, _: String -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},
    login: String? = null,
    error: String? = null,
) {

    val isLargeScreen =
        LocalConfiguration.current.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val maxWidth = 320.dp
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var username by rememberSaveable { mutableStateOf(login ?: "") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordState by rememberSaveable { mutableStateOf(false) }

    if (!isLargeScreen && isLandscape) {
        SmallScreenLandscapeLogin(
            modifier = modifier,
            maxWidth = maxWidth,
            keyboardController = keyboardController,
            focusManager = focusManager,
            username = username,
            onUsernameChanged = { username = it },
            password = password,
            onPasswordChanged = { password = it },
            passwordState = passwordState,
            onPasswordStateChanged = { passwordState = it },
            enabled = enabled,
            onLogin = onLogin,
            onForgotPassword = onForgotPassword,
            onSignUp = onSignUp,
            error = error
        )
    } else {
        Login(
            modifier = modifier,
            maxWidth = maxWidth,
            keyboardController = keyboardController,
            focusManager = focusManager,
            username = username,
            onUsernameChanged = { username = it },
            password = password,
            onPasswordChanged = { password = it },
            passwordState = passwordState,
            onPasswordStateChanged = { passwordState = it },
            enabled = enabled,
            onLogin = onLogin,
            onForgotPassword = onForgotPassword,
            onSignUp = onSignUp,
            error = error
        )
    }
}

@Composable
private fun Login(
    modifier: Modifier,
    maxWidth: Dp,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    username: String,
    onUsernameChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    passwordState: Boolean,
    onPasswordStateChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
    onLogin: (username: String, password: String) -> Unit = { _: String, _: String -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},
    error: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_new_nordic_logo),
                contentDescription = stringResource(R.string.name_nordic),
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp),
            )
            Image(
                painter = painterResource(R.drawable.ic_edge_impulse),
                contentDescription = stringResource(R.string.name_edge_impulse),
                modifier = Modifier
                    .size(64.dp),
            )
        }

        error?.let { message ->
            Text(
                text = stringResource(R.string.label_login_error, message),
                modifier = Modifier.padding(bottom = 16.dp),
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedTextField(
            value = username,
            onValueChange = { onUsernameChanged(it.trim()) },
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
            enabled = enabled,
            label = { Text(stringResource(R.string.field_username)) },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChanged(it) },
            modifier = Modifier
                .padding(top = 8.dp)
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
            enabled = enabled,
            label = { Text(stringResource(R.string.field_password)) },
            visualTransformation = if (passwordState) VisualTransformation.None
            else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(
                    onClick = { onPasswordStateChanged(!passwordState) }
                ) {
                    Icon(
                        imageVector = if (passwordState)
                            Icons.Outlined.Visibility
                        else Icons.Outlined.VisibilityOff,
                        contentDescription = stringResource(R.string.action_show_password)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                onLogin(username, password)
            },
            singleLine = true
        )
        Text(
            text = stringResource(R.string.action_forgot_password),
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clickable(enabled = enabled, onClick = onForgotPassword),
            color = NordicBlue,
            textAlign = TextAlign.End,
        )
        Button(
            onClick = {
                keyboardController?.hide()
                onLogin(username, password)
            },
            modifier = Modifier
                .height(46.dp)
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
            enabled = enabled,
        ) {
            if (enabled) {
                Text(
                    text = stringResource(R.string.action_login),
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.label_no_account))
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Text(
                text = stringResource(R.string.action_signup),
                color = NordicBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = enabled, onClick = onSignUp)
            )
        }
    }
}

@Composable
private fun SmallScreenLandscapeLogin(
    modifier: Modifier,
    maxWidth: Dp,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    username: String,
    onUsernameChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    passwordState: Boolean,
    onPasswordStateChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
    onLogin: (username: String, password: String) -> Unit = { _: String, _: String -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},
    error: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = modifier
                .weight(1.0f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Row(
                Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_new_nordic_logo),
                    contentDescription = stringResource(R.string.name_nordic),
                    modifier = Modifier
                        .size(128.dp)
                        .padding(8.dp),
                )
                Image(
                    painter = painterResource(R.drawable.ic_edge_impulse),
                    contentDescription = stringResource(R.string.name_edge_impulse),
                    modifier = Modifier
                        .size(128.dp),
                )
            }
        }
        Column(
            modifier = modifier
                .weight(1.5f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            error?.let { message ->
                Text(
                    text = stringResource(R.string.label_login_error, message),
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            OutlinedTextField(
                value = username,
                onValueChange = { onUsernameChanged(it.trim()) },
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
                enabled = enabled,
                label = { Text(stringResource(R.string.field_username)) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = { onPasswordChanged(it) },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
                enabled = enabled,
                label = { Text(stringResource(R.string.field_password)) },
                visualTransformation = if (passwordState) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { onPasswordStateChanged(!passwordState) }) {
                        Icon(
                            imageVector = if (passwordState) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                            contentDescription = stringResource(R.string.action_show_password)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions {
                    keyboardController?.hide()
                    onLogin(username, password)
                },
                singleLine = true
            )
            Text(
                text = stringResource(R.string.action_forgot_password),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clickable(enabled = enabled, onClick = onForgotPassword),
                color = NordicBlue,
                textAlign = TextAlign.End,
            )
            Button(
                onClick = {
                    keyboardController?.hide()
                    onLogin(username, password)
                },
                modifier = Modifier
                    .height(46.dp)
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
                enabled = enabled,
            ) {
                if (enabled) {
                    Text(
                        text = stringResource(R.string.action_login),
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = stringResource(R.string.label_no_account))
                Spacer(modifier = Modifier.padding(start = 4.dp))
                Text(
                    text = stringResource(R.string.action_signup),
                    color = NordicBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = enabled, onClick = onSignUp)
                )
            }
        }
    }
}

@Preview(name = "Dark")
@Composable
fun LoginPreviewDark() {
    NordicTheme {
        Surface {
            Login(error = "Invalid password")
        }
    }
}

@Preview(name = "Light")
@Composable
fun LoginPreviewLight() {
    NordicTheme {
        Surface {
            Login(error = "Invalid password")
        }
    }
}