package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    onRefresh: (Boolean, Boolean) -> Unit,
    onCreateNewProject: (String) -> Unit,
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
    var isFirstItemVisible by rememberSaveable { mutableStateOf(true) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        CreateProjectDialog(onConfirm = onCreateNewProject, onDismiss = {
            showDialog = false
        })
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
            if (isFirstItemVisible) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(text = stringResource(R.string.action_create_project))
                    },
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(16.dp),
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.content_description_create_project)
                        )
                    }
                )
            } else {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(16.dp),
                    content = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.content_description_create_project)
                        )
                    })
            }
        }
    ) { innerPadding ->
        var isScrolling by remember { mutableStateOf(false) }
        SwipeToRefreshLayout(
            refreshingState = refreshState,
            onRefresh = { onRefresh(isScrolling, isFirstItemVisible) },
            refreshIndicator = {
                if (!isScrolling && isFirstItemVisible)
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
                    projects = user.projects,
                    isFirstItemVisible = {
                        isFirstItemVisible = it
                    },
                    isScrolling = {
                        isScrolling = it
                    }
                )
            })
    }
}

@Composable
fun ProjectsList(
    modifier: Modifier = Modifier,
    projects: List<Project>,
    isScrolling: (Boolean) -> Unit,
    isFirstItemVisible: (Boolean) -> Unit
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
            isScrolling(scrollState.firstVisibleItemScrollOffset < -5)
            isFirstItemVisible(scrollState.firstVisibleItemIndex == 0)
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
                Image(
                    painter = painterResource(id = R.drawable.ic_project_diagram),
                    contentDescription = null
                )
            },
            loading = {
                Image(
                    painter = painterResource(id = R.drawable.ic_project_diagram),
                    contentDescription = null,
                    alpha = 0.1f
                )
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

@Composable
private fun CreateProjectDialog(
    modifier: Modifier = Modifier,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = modifier
                .requiredWidth(400.dp)
                .background(MaterialTheme.colors.surface)
                .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_project_diagram),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.label_create_project),
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = modifier.padding(top = 8.dp, bottom = 8.dp),
                text = stringResource(R.string.label_enter_project_name),
                color = MaterialTheme.colors.onSurface
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                label = { Text(stringResource(R.string.field_project_name)) },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Next,
                ),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(textColor = MaterialTheme.colors.onSurface)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = { onDismiss() }) {
                    Text(text = "CANCEL")
                }
                TextButton(
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = {
                        onConfirm(name)
                        onDismiss()
                    }) {
                    Text(text = "CREATE")
                }
            }
        }
    }
}