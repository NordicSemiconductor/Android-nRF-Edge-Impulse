package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ShowAlertDialog


@Composable
internal fun DeleteUser(
    email: String = "",
    isMfaConfigured: Boolean = false,
    deleteUser: suspend (String, String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordState by rememberSaveable { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var deleteUserRequested by remember { mutableStateOf(false) }
    ShowAlertDialog(
        imageVector = Icons.Outlined.Info,
        title = stringResource(id = R.string.action_delete_user),
        text = {
            Column {
                Text(
                    text = stringResource(R.string.label_delete_user_confirmation),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_email_address)) },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    enabled = false,
                    singleLine = true
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_password)) },
                    visualTransformation = if (passwordState) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordState = !passwordState }) {
                            Icon(
                                imageVector = if (passwordState) Icons.Outlined.Visibility
                                else Icons.Outlined.VisibilityOff,
                                contentDescription = stringResource(R.string.action_show_password)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        imeAction = if (isMfaConfigured) ImeAction.Next else ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            deleteUserRequested = true
                        }
                    ),
                    singleLine = true
                )
                if (isMfaConfigured) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        label = { Text(stringResource(R.string.label_authenticator_code)) },
                        leadingIcon = { Icon(Icons.Outlined.Pin, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = false,
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                deleteUserRequested = true
                            }
                        ),
                        singleLine = true
                    )
                }
            }
        },
        dismissText = stringResource(id = R.string.action_cancel),
        onDismiss = onDismiss,
        confirmText = stringResource(id = R.string.action_delete),
        onConfirm = {
            if (isMfaConfigured) {
                if (code.isNotBlank() && password.isNotBlank()) {
                    deleteUserRequested = true
                }
            } else {
                deleteUserRequested = true
            }
        },
        isDestructive = true
    )
    if (deleteUserRequested) {
        LaunchedEffect(Unit) {
            deleteUser(password, code)
        }
    }
}