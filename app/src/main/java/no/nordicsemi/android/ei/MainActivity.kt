package no.nordicsemi.android.ei

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import no.nordicsemi.android.ei.ui.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
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