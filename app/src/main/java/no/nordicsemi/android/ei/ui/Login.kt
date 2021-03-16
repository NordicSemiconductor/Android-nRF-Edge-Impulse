package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.theme.NordicBlue

@ExperimentalCoroutinesApi
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
    var username by rememberSaveable { mutableStateOf(login ?: "") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordState by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.padding(vertical = 16.dp)
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
                        modifier = Modifier.size(64.dp),
                    )
                }

                error?.let { message ->
                    Text(text = stringResource(R.string.label_login_error, message))
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { onUsernameChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    label = { Text(stringResource(R.string.field_username)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { onPasswordChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = enabled,
                    label = { Text(stringResource(R.string.field_password)) },
                    visualTransformation = if (passwordState) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            passwordState = !passwordState
                        }) {
                            Icon(
                                painter = painterResource(
                                    if (passwordState) R.drawable.ic_baseline_visibility_24
                                    else R.drawable.ic_baseline_visibility_off_24
                                ),
                                contentDescription = stringResource(R.string.action_show_password)
                            )
                        }
                    },
                    singleLine = true
                )
                Text(
                    text = stringResource(R.string.action_forgot_password),
                    modifier = Modifier
                        .clickable(enabled = enabled, onClick = onForgotPassword)
                        .padding(vertical = 8.dp),
                    color = NordicBlue,
                    textAlign = TextAlign.End,
                )
                Button(
                    onClick = { onLogin(username, password) },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .height(46.dp)
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
                        modifier = Modifier.clickable(enabled = enabled,onClick = onSignUp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    NordicTheme(darkTheme = false) {
        Login(error = "Invalid password")
    }
}