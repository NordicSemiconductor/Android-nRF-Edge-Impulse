package no.nordicsemi.android.ei

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.ui.theme.NordicTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme(darkTheme = false) {
                Screen(name = title.toString())
            }
        }
    }
}

@Composable
fun Screen(name: String) {
    var title by remember { mutableStateOf(name) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title)
                }
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) {
        Navigation(
            Modifier.padding(16.dp),
            onTitleChange = { title = it }
        )
    }
}