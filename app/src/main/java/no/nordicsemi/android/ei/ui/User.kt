package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.coil.CoilImage
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.ui.layouts.SwipeToRefreshLayout

@Composable
fun User(
    user: User,
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    onLogoutClick: () -> Unit
) {

    val height: Dp = 150.dp
    Scaffold {
        Column {
            UserContent(
                height = height,
                user = user,
                onLogoutClick = onLogoutClick
            )
            Projects(
                height = height,
                projects = user.projects,
                refreshingState = refreshingState,
                onRefresh = { onRefresh() }
            )
        }
    }
}

@Composable
fun UserContent(
    modifier: Modifier = Modifier,
    height: Dp,
    user: User,
    onLogoutClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 16.dp)
            .background(color = MaterialTheme.colors.primary)
    ) {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    text = stringResource(R.string.label_welcome),
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier
                        .clickable(
                            onClickLabel = stringResource(id = R.string.action_logout),
                            role = Role.Button,
                            onClick = onLogoutClick
                        )
                        .clip(CircleShape)
                        .padding(16.dp),
                    text = stringResource(R.string.action_logout),
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }
        Row(
            modifier = modifier
                .offset(y = height / 2)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = modifier
                    .padding(start = 16.dp)
                    .size(150.dp)
                    .border(
                        border = BorderStroke(4.dp, color = Color.White),
                        shape = CircleShape
                    ),
                shape = CircleShape,
                elevation = 10.dp
            ) {
                CoilImage(
                    data = user.photo ?: R.drawable.ic_edge_impulse,
                    contentDescription = stringResource(R.string.content_description_user_image),
                    modifier = modifier.size(50.dp),
                    alignment = Alignment.Center
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(
                    modifier = Modifier
                        .size(16.dp)
                )
                Text(
                    color = MaterialTheme.colors.onPrimary,
                    text = user.name,
                    style = MaterialTheme.typography.h6,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    color = MaterialTheme.colors.onBackground,
                    text = user.email,
                    style = MaterialTheme.typography.subtitle2,
                    maxLines = 1
                )
                Text(
                    color = MaterialTheme.colors.onSurface,
                    text = user.username,
                    style = MaterialTheme.typography.subtitle2,
                    maxLines = 1
                )
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun Projects(
    height: Dp,
    modifier: Modifier = Modifier,
    projects: List<Project>,
    refreshingState: Boolean,
    onRefresh: () -> Unit
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
                height = height,
                modifier = modifier,
                projects = projects
            )
        })
}

@Composable
fun ProjectList(
    height: Dp,
    modifier: Modifier,
    projects: List<Project>
) {

    val scrollState = rememberLazyListState()
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = height / 2)
        )
        Column {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp),
                text = stringResource(id = R.string.title_projects),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.h6
            )
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 16.dp),
                state = scrollState
            ) {
                itemsIndexed(items = projects) { _, project ->
                    ProjectRow(
                        modifier = modifier,
                        project = project
                    )
                }
            }
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