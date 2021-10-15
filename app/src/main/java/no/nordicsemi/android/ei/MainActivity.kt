package no.nordicsemi.android.ei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.ui.theme.NordicTheme

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
fun ShowDialog(
    @DrawableRes drawableRes: Int,
    title: String,
    onDismissed: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissed,
        properties = properties,
        content = {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape = RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = drawableRes),
                            contentDescription = null,
                            tint = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    content()
                }
            }
        }
    )
}

@Composable
fun ShowDialog(
    imageVector: ImageVector,
    title: String,
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape = RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = imageVector,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    content()
                }
            }
        }
    )
}

@Composable
fun ShowDataAcquisitionDialog(
    imageVector: ImageVector,
    title: String,
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape = RoundedCornerShape(4.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(start = 24.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = imageVector,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    content()
                }
            }
        }
    )
}