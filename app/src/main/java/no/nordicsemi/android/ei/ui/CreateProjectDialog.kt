package no.nordicsemi.android.ei.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.service.param.ProjectVisibility
import no.nordicsemi.android.ei.ui.layouts.AlertDialog


@Composable
fun CreateProjectDialog(
    onCreateProject: (String, ProjectVisibility) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isError by rememberSaveable { mutableStateOf(false) }
    var projectName by rememberSaveable { mutableStateOf("") }
    var isCreateClicked by rememberSaveable { mutableStateOf(false) }
    var projectVisibility by remember { mutableStateOf(ProjectVisibility.PRIVATE) }
    AlertDialog(
        imageVector = Icons.Outlined.Share,
        title = stringResource(id = R.string.dialog_title_create_project),
        text = {
            Column {
                Text(
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    text = stringResource(R.string.label_enter_project_name),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(state = rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = {
                            projectName = it
                            isError = projectName.isBlank()
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester = focusRequester)
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
                        label = { Text(stringResource(R.string.field_project_name)) },
                        isError = isError,
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = false,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusRequester.freeFocus()
                            keyboardController?.hide()
                        }),
                        singleLine = true,
                    )
                    if (isError) {
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = stringResource(R.string.label_empty_project_name_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(modifier = Modifier.selectableGroup()) {
                        Text(
                            text = "Select project visibility",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        ProjectVisibility.entries.forEach { entry ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (entry == projectVisibility),
                                        onClick = { projectVisibility = entry },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (entry == projectVisibility),
                                    onClick = null // null recommended for accessibility with screen readers
                                )
                                Text(
                                    text = entry.displayString(LocalContext.current),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                }
            }
        },
        dismissText = stringResource(id = R.string.action_cancel),
        onDismiss = onDismiss,
        confirmText = stringResource(id = R.string.action_create),
        onConfirm = {
            isCreateClicked = !isCreateClicked
            onCreateProject(projectName, projectVisibility)
        }
    )
}

private fun ProjectVisibility.displayString(context: Context) = when (this) {
    ProjectVisibility.PRIVATE -> context.getString(R.string.label_private)
    ProjectVisibility.PUBLIC -> context.getString(R.string.label_public)
}