package no.nordicsemi.android.ei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.ui.theme.NordicTheme
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme {
                Navigation(
                    onError = { finish() }
                )
            }
        }
    }
}

fun showSnackbar(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    coroutineScope.launch {
        snackbarHostState.showSnackbar(message = message)
    }
}

@Composable
fun ShowWarningDialog(
    message: String,
    onDismissed: () -> Unit,
    onContinue: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismissed() },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        content = {
            Surface(modifier = Modifier.wrapContentSize()) {
                Column(modifier = Modifier.padding(start = 24.dp, end = 8.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.label_warning).uppercase(Locale.US),
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .verticalScroll(state = rememberScrollState())
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.body1
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { onDismissed() }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_cancel).uppercase(
                                        Locale.US
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { onContinue() }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_dialog_continue).uppercase(
                                        Locale.US
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}