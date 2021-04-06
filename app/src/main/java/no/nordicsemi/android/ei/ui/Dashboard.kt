package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.coil.CoilImage
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.ui.layouts.SwipeToRefreshLayout
import no.nordicsemi.android.ei.ui.layouts.UserAppBar
import java.net.UnknownHostException

@Composable
fun Dashboard(
    user: User,
    refreshState: Boolean,
    error: Throwable?,
    onRefresh: () -> Unit,
    onCreateNewProject: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    error?.let { throwable ->
        val message = when (throwable) {
            is UnknownHostException -> stringResource(id = R.string.error_no_internet)
            else -> throwable.localizedMessage ?: stringResource(id = R.string.error_refreshing_failed)
        }
        LaunchedEffect(throwable) {
            snackbarHostState.showSnackbar(message)
        }
    }
    Scaffold(
        scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState),
        topBar = {
            UserAppBar(
                title = {
                    Text(text = stringResource(id = R.string.label_welcome))
                },
                user = user,
                onLogoutClick = onLogoutClick,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { 
                    Text(text = "Create new project")
                },
                onClick = onCreateNewProject,
                icon = {
                    Icon(Icons.Default.Add, contentDescription = "Create new project")
                }
            )
        }
    ) { innerPadding ->
        SwipeToRefreshLayout(
            refreshingState = refreshState,
            onRefresh = onRefresh,
            refreshIndicator = {
                Surface(elevation = 10.dp, shape = CircleShape) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp),
                        strokeWidth = 2.dp,
                    )
                }
            },
            content = {
                ProjectsList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    projects = user.projects
                )
            })
    }
}

@Composable
fun ProjectsList(
    modifier: Modifier = Modifier,
    projects: List<Project>
) {
    projects.takeIf { it.isNotEmpty() }?.let { notEmptyProjects ->
        val scrollState = rememberLazyListState()
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(top = 72.dp, bottom = 36.dp),
            state = scrollState
        ) {
            item {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    text = stringResource(id = R.string.title_projects),
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.h6
                )
            }
            items(
                items = notEmptyProjects,
                key = { project -> project.id }
            ) { project ->
                ProjectRow(project = project)
                Divider(modifier = Modifier.width(Dp.Hairline))
            }
        }
    } ?: run {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_project_diagram),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.label_no_projects),
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }
}

@Composable
fun ProjectRow(
    modifier: Modifier = Modifier,
    project: Project
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colors.surface)
            .clickable { }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoilImage(
            data = project.logo ?: R.drawable.ic_project_diagram,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp),
            error = {
                Image(painter = painterResource(id = R.drawable.ic_project_diagram), contentDescription = null)
            },
            loading = {
                Image(painter = painterResource(id = R.drawable.ic_project_diagram), contentDescription = null, alpha = 0.1f)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1.0f),
            text = project.name,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}