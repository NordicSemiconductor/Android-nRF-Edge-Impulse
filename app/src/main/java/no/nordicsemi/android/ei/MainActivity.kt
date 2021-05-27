package no.nordicsemi.android.ei

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.SnackbarHostState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.ui.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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