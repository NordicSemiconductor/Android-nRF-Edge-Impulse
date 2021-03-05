package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import no.nordicsemi.android.ei.ui.theme.NordicTheme

@Composable
fun Login(
    modifier: Modifier = Modifier,
    navigation: NavHostController
) {
    Button(
        onClick = {
            navigation.navigate("projects")
        },
        modifier = modifier.padding(32.dp)
    ) {
        Text(text = "Log in")
    }
}