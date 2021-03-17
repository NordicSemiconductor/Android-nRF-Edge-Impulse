package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.ui.layouts.SwipeToRefreshLayout

@Composable
fun Projects(
    modifier: Modifier = Modifier,
    projects: List<Project>,
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (Int) -> Unit
) {
    SwipeToRefreshLayout(
        refreshingState = refreshingState,
        onRefresh = onRefresh,
        refreshIndicator = {
            Surface(elevation = 10.dp, shape = CircleShape) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp)
                )
            }
        },
        content = {
            ProjectList(
                modifier = modifier,
                projects = projects,
                onItemClick = onItemClick
            )
        })
}

@Composable
fun ProjectList(
    modifier: Modifier,
    projects: List<Project>,
    onItemClick: (Int) -> Unit
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(bottom = 16.dp),
        state = scrollState
    ) {
        itemsIndexed(items = projects) { _, project ->
            ProjectRow(
                modifier = modifier,
                project = project,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
fun ProjectRow(
    modifier: Modifier = Modifier,
    project: Project,
    onItemClick: (Int) -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = { onItemClick(project.id) }),
        shape = MaterialTheme.shapes.large,
    ) {
        Column {
            Row(
                modifier
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_project_diagram),
                    contentDescription = stringResource(R.string.name_project_logo),
                    modifier = Modifier
                        .size(36.dp)
                        .padding(8.dp),
                    alignment = Alignment.Center
                )
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(modifier = Modifier.fillMaxWidth(), text = project.name)
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            stringResource(id = R.string.id, formatArgs = arrayOf(project.id)),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
            Divider(
                color = Color.LightGray, modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .alpha(0.1f)
            )
        }
    }
}