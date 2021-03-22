package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.*
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.ui.layouts.SwipeToRefreshLayout
import no.nordicsemi.android.ei.ui.layouts.UserAppBar

@Composable
fun User(
    user: User,
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            UserAppBar(
                title = {
                    Text(text = stringResource(id = R.string.label_welcome))
                },
                user = user,
                onLogoutClick = onLogoutClick,
            )
        }
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
                ProjectsList(
                    modifier = Modifier.fillMaxSize(),
                    projects = user.projects
                )
            })
    }
}

@Composable
fun ProjectsList(
    modifier: Modifier,
    projects: List<Project>
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier,
        state = scrollState
    ) {
        item {
            Text(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .padding(top = 72.dp, bottom = 16.dp),
                text = stringResource(id = R.string.title_projects),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.h6
            )
        }
        items(items = projects) { project ->
            ProjectRow(
                modifier = modifier,
                project = project
            )
        }
    }
}

@Composable
fun ProjectRow(
    modifier: Modifier = Modifier,
    project: Project
) {
    Card(
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