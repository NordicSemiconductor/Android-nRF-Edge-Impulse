package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.theme.NordicBlue

@ExperimentalCoroutinesApi
@Composable
fun Login(
    modifier: Modifier = Modifier,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: (username: String, password: String) -> Unit
) {
    var passwordState by remember { mutableStateOf(false) }

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
                        painter = painterResource(R.drawable.ic_edge_impulse_mark_rgb),
                        contentDescription = stringResource(R.string.name_edge_impulse),
                        modifier = Modifier.size(64.dp),
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { onUsernameChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_username)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { onPasswordChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
                        .fillMaxWidth()
                        .clickable(enabled = true, onClick = {/*TODO*/ })
                        .padding(top = 8.dp),
                    color = NordicBlue,
                    textAlign = TextAlign.End
                )
                Button(
                    onClick = {
                        onLogin(username, password)
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.action_login),
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 4.dp,
                            end = 16.dp,
                            bottom = 4.dp
                        )
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = stringResource(R.string.label_no_account))
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text(text = stringResource(R.string.action_signup), color = NordicBlue)
                }
            }
        }
    }
}