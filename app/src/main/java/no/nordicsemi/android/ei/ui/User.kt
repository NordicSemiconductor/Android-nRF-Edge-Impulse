package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
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
    Scaffold(
        topBar = {
            UserAppBar(
                title = {
                    Text(text = stringResource(id = R.string.label_welcome))
                },
                user = user,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
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
fun UserAppBar(
    title: @Composable () -> Unit,
    user: User,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onLogoutClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
    ) {
        TopAppBar(
            modifier = Modifier.fillMaxHeight(0.66f),
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            elevation = elevation
        ) {
            Spacer(modifier = Modifier.width(12.dp)) // 16.dp - 4.dp
            Row(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ProvideTextStyle(value = MaterialTheme.typography.h6) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                        content = title
                    )
                }
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Row(
                    Modifier.fillMaxHeight(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Top,
                    content = {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More actions")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(x = 0.dp, y = (-48).dp)
                        ) {
                            DropdownMenuItem(onClick = onLogoutClick) {
                                Text(text = stringResource(id = R.string.action_logout))
                            }
                        }
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 48.dp, end = 16.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .aspectRatio(1.0f)
                    .border(
                        border = BorderStroke(3.dp, color = MaterialTheme.colors.onPrimary),
                        shape = CircleShape
                    ),
                shape = CircleShape,
                elevation = 10.dp
            ) {
                CoilImage(
                    data = user.photo ?: R.drawable.ic_edge_impulse,
                    contentDescription = stringResource(R.string.content_description_user_image),
                    alignment = Alignment.Center
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 4.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = user.name,
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp),
                    text = user.email,
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.caption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

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
                    .padding(vertical = 16.dp),
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