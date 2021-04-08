package no.nordicsemi.android.ei.ui

import android.util.Log
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.flowWithLifecycle
import dev.chrisbanes.accompanist.coil.CoilImage
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.service.param.CreateProjectResponse
import no.nordicsemi.android.ei.ui.layouts.SwipeToRefreshLayout
import no.nordicsemi.android.ei.ui.layouts.UserAppBar
import no.nordicsemi.android.ei.viewmodels.DashboardViewModel
import java.net.UnknownHostException

@Composable
fun Dashboard(
    viewModel: DashboardViewModel,
    onLogout: (Unit) -> Unit
) {
    val user = viewModel.user
    val refreshState = viewModel.isRefreshing
    val error by viewModel.error
        .flowWithLifecycle(LocalLifecycleOwner.current.lifecycle)
        .collectAsState(initial = null)
    val createProjectResponse by viewModel.createProjectResponse
        .flowWithLifecycle(LocalLifecycleOwner.current.lifecycle)
        .collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }
    error?.let { throwable ->
        ShowSnackbar(
            snackbarHostState = snackbarHostState, key = throwable, message = when (throwable) {
                is UnknownHostException -> stringResource(id = R.string.error_no_internet)
                else -> throwable.localizedMessage
                    ?: stringResource(id = R.string.error_refreshing_failed)
            }
        )
    }
    var isFirstItemVisible by rememberSaveable { mutableStateOf(true) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        CreateProjectDialog(
            createProjectResponse = createProjectResponse,
            onCreateProject = { projectName ->
                viewModel.createProject(projectName)
            },
            onDismiss = {
                showDialog = it
            })
    } else {
        createProjectResponse?.let { response ->
            ShowSnackbar(
                snackbarHostState = snackbarHostState,
                key = response,
                message = when (response.success) {
                    true -> stringResource(id = R.string.project_created_successfully)
                    false -> stringResource(
                        id = R.string.error_generic,
                        response.error ?: stringResource(id = R.string.project_created_error)
                    )
                }
            )
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
                onLogoutClick = {
                    onLogout(viewModel.logout())
                },
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
                            contentDescription = null
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
                            contentDescription = null
                        )
                    })
            }
        }
    ) { innerPadding ->
        var isScrolling by remember { mutableStateOf(false) }
        SwipeToRefreshLayout(
            refreshingState = refreshState,
            onRefresh = {
                if (!isScrolling && isFirstItemVisible)
                    viewModel.refreshUser()
            },
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
    createProjectResponse: CreateProjectResponse?,
    onCreateProject: (String) -> Unit,
    onDismiss: (Boolean) -> Unit
) {
    var projectName by rememberSaveable { mutableStateOf("") }
    var isCreateClicked by rememberSaveable { mutableStateOf(false) }
    Dialog(
        onDismissRequest = {
            Log.i("AA", "On dismiss request")
            onDismiss(false)
        },
        properties =
        if (isCreateClicked) {
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        } else DialogProperties()
    ) {
        Column(
            modifier = modifier
                .background(MaterialTheme.colors.surface)
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
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
                    text = stringResource(id = R.string.dialog_title_create_project),
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
                value = projectName,
                onValueChange = { projectName = it },
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
                    onClick = { onDismiss(false) }) {
                    Text(text = stringResource(R.string.action_dialog_cancel))
                }
                TextButton(
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = {
                        isCreateClicked = !isCreateClicked
                        onCreateProject(projectName)
                        createProjectResponse?.let {
                            onDismiss(false)
                        }
                    },
                    enabled = !isCreateClicked
                ) {
                    Text(text = stringResource(R.string.action_dialog_create))
                }
            }
        }
    }
}

@Composable
private fun ShowSnackbar(
    snackbarHostState: SnackbarHostState,
    key: Any,
    message: String
) {
    LaunchedEffect(key1 = key) {
        snackbarHostState.showSnackbar(message = message)
    }
}