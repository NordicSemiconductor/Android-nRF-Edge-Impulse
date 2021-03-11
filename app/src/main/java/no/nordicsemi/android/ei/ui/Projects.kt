package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Project

@Composable
fun Projects(
    modifier: Modifier = Modifier,
    token: String,
    onRefresh: (token: String) -> Unit,
    projects: List<Project>
) {

    LazyColumn(Modifier.fillMaxWidth()) {
        itemsIndexed(items = projects) { _, project ->
            ProjectRow(modifier = modifier, project = project)
        }
    }
}

@Composable
fun ProjectRow(modifier: Modifier = Modifier, project: Project) {
    Column(
        Modifier.background(color = MaterialTheme.colors.surface)
    ) {
        Row(
            modifier
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colors.primary
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_baseline_developer_board_24),
                    contentDescription = stringResource(R.string.name_project_logo),
                    modifier = Modifier
                        .padding(8.dp),
                    alignment = Alignment.Center
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(modifier = Modifier.fillMaxWidth(), text = project.name)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(project.id.toString(), style = MaterialTheme.typography.body2)
                }
            }
        }
        Divider(
            color = Color.LightGray, modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}