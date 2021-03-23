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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    ) { innerPadding ->
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
    val scrollState = rememberLazyListState()
    val horizontalPadding =
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
        items(items = projects) { project ->
            ProjectRow(project = project)
            Divider(modifier = Modifier.width(Dp.Hairline))
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
            contentDescription = stringResource(R.string.content_description_project_logo),
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