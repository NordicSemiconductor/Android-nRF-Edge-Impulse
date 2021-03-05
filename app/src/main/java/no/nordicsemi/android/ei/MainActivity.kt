package no.nordicsemi.android.ei

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import no.nordicsemi.android.ei.ui.theme.NordicTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme(darkTheme = false) {
                Screen()
            }
        }
    }
}

@Composable
fun Screen() {
    var topBarVisible by remember { mutableStateOf(false) }
    var topBarTitle by remember { mutableStateOf("")}
    Scaffold(
        topBar = {
            if (topBarVisible) {
                TopAppBar(
                    title = { Text(text = topBarTitle) }
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background
    ) {
        Navigation(
            tabBarSpec = { visible,  title ->
                topBarVisible = visible
                topBarTitle = title ?: ""
            }
        )
    }
}