package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R

@Composable
fun Devices(modifier: Modifier) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
        item {
            Text(
                modifier = modifier.padding(16.dp),
                text = stringResource(R.string.label_devices),
                style = MaterialTheme.typography.h6
            )
            Text(
                modifier = modifier.padding(16.dp),
                text = stringResource(R.string.label_scanner),
                style = MaterialTheme.typography.h6
            )
        }
    }
}