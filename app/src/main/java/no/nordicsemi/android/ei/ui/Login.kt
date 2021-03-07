package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.theme.NordicBlue
import no.nordicsemi.android.ei.ui.theme.NordicBlueDark

@Composable
fun Login(
    modifier: Modifier = Modifier,
    navigation: NavHostController,
) {
    LoginSection(modifier)
}

@Composable
fun LoginSection(modifier: Modifier) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var passwordState: Boolean by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(color = NordicBlueDark),
        verticalArrangement = Arrangement.Center
    ) {
        Card(modifier = modifier.padding(16.dp)) {
            Column(
                modifier = modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_new_nordic_logo),
                            contentDescription = "Nordic Semiconductor",
                            modifier = modifier.padding(8.dp)
                        )
                    }
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_edge_impulse_mark_rgb),
                            contentDescription = "Edge Impulse"
                        )
                    }
                }

                OutlinedTextField(
                    modifier = modifier
                        .fillMaxWidth(),
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text("Password") },
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
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    singleLine = true
                )
                Text(
                    text = "Forgot password?", modifier = modifier
                        .fillMaxWidth()
                        .clickable(enabled = true, onClick = {/*TODO*/ })
                        .padding(top = 8.dp),
                    color = NordicBlue,
                    textAlign = TextAlign.End
                )
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Login",
                        modifier = modifier.padding(
                            start = 16.dp,
                            top = 4.dp,
                            end = 16.dp,
                            bottom = 4.dp
                        )
                    )
                }
                Row(
                    modifier = modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Don't have an account yet?")
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text(text = "Sign Up", color = NordicBlue)
                }
            }
        }
    }
}