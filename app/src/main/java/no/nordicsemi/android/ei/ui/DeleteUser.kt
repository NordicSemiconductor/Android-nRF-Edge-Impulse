package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.theme.view.NordicAppBar
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.viewmodels.DeleteUserViewModel
import no.nordicsemi.android.ei.viewmodels.state.DeleteState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteUser(viewModel: DeleteUserViewModel, onDeleted: () -> Unit, onBackPressed: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            NordicAppBar(
                showBackButton = true,
                text = stringResource(id = R.string.action_delete_user),
                onNavigationButtonClick = {
                    if (state != DeleteState.Deleting) {
                        onBackPressed()
                    }
                }
            )
        }
    ) {
        when (state) {
            DeleteState.Deleted -> {
                LaunchedEffect(state) {
                    onDeleted()
                }
            }

            else -> LazyColumn(contentPadding = it) {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = stringResource(R.string.title_warning),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Surface(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .padding(all = 16.dp)
                            ) {
                                Text(text = stringResource(R.string.label_delete_user_irrevocable))
                            }
                        }
                    }
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = stringResource(R.string.label_projects_to_be_deleted),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(
                        items = viewModel.user.projects,
                        key = { project -> project.id }
                    ) { project ->
                        ProjectRow(
                            project = project
                        )
                        HorizontalDivider()
                    }
                    item {
                        DeleteUserContent(
                            state = state,
                            user = viewModel.user,
                            deleteUser = { password, code ->
                                viewModel.deleteUser(password, code)
                            }
                        )
                    }
                }
        }
    }

    LaunchedEffect(state) {
        if (state is DeleteState.Error) {
            snackbarHostState.showSnackbar(
                message = (state as DeleteState.Error).throwable.message ?: "Error"
            )
        }
    }
}

@Composable
private fun DeleteUserContent(
    state: DeleteState,
    user: User,
    deleteUser: (String, String?) -> Unit
) {
    var errorInPasswordField by remember { mutableStateOf(false) }
    var errorInCodeField by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordState by rememberSaveable { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var deleteUserRequested by remember { mutableStateOf(false) }

    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        text = stringResource(R.string.how_to_delete),
        style = MaterialTheme.typography.titleLarge
    )
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(all = 16.dp)
        ) {
            Text(text = stringResource(R.string.label_delete_user_rationale))
            Column {
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = user.email,
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_email_address)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null
                        )
                    },
                    enabled = false,
                    singleLine = true
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorInPasswordField = false
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_password)) },
                    visualTransformation = when {
                        passwordState -> VisualTransformation.None
                        else -> PasswordVisualTransformation()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordState = !passwordState }) {
                            Icon(
                                imageVector = when {
                                    passwordState -> Icons.Outlined.Visibility
                                    else -> Icons.Outlined.VisibilityOff
                                },
                                contentDescription = stringResource(R.string.action_show_password)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        imeAction = when {
                            user.mfaConfigured -> ImeAction.Next
                            else -> ImeAction.Done
                        },
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true,
                    enabled = state != DeleteState.Deleting,
                    isError = errorInPasswordField
                )
                if (user.mfaConfigured) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                            errorInCodeField = false
                        },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        label = { Text(stringResource(R.string.label_authenticator_code)) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Pin,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = false,
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        enabled = state != DeleteState.Deleting,
                        isError = errorInCodeField
                    )
                }
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            modifier = Modifier
                .size(width = 320.dp, height = 46.dp),
            enabled = state != DeleteState.Deleting,
            onClick = {
                errorInPasswordField = password.isEmpty()
                if (user.mfaConfigured) {
                    errorInCodeField = code.isEmpty()
                    if (!errorInPasswordField && !errorInCodeField) {
                        deleteUser(password, code)
                    }
                } else {
                    if (!errorInPasswordField) {
                        deleteUser(password, null)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(Color.Red)
        ) {
            if (state == DeleteState.Deleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(
                    modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                    text = stringResource(R.string.action_delete),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
    }
}