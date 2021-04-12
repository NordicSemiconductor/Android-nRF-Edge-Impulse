package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.ui.layouts.SwipeToRefreshLayout
import no.nordicsemi.android.ei.ui.layouts.UserAppBar
import no.nordicsemi.android.ei.viewmodels.DashboardViewModel
import no.nordicsemi.android.ei.viewmodels.event.Event
import java.net.UnknownHostException

@Composable
fun Dashboard(
    viewModel: DashboardViewModel,
    onLogout: (Unit) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = LocalLifecycleOwner.current.lifecycleScope
    val snackbarHostState = remember { SnackbarHostState() }
    val user = viewModel.user
    val refreshState = viewModel.isRefreshing

    var isFirstItemVisible by rememberSaveable { mutableStateOf(true) }
    var isCreateProjectDialogVisible by rememberSaveable { mutableStateOf(false) }

    coroutineScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect {
                when (it) {
                    is Event.DismissDialog -> {
                        isCreateProjectDialogVisible = false
                    }
                    is Event.ProjectCreated -> {
                        showSnackbar(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            message = context.getString(
                                R.string.project_created_successfully,
                                it.projectName
                            )
                        )
                    }
                    is Event.Error -> {
                        showSnackbar(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            message = when (it.throwable) {
                                is UnknownHostException -> context.getString(R.string.error_no_internet)
                                else -> it.throwable.localizedMessage
                                    ?: context.getString(R.string.error_refreshing_failed)
                            }
                        )
                    }
                }
            }
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
            CreateProjectFloatingActionButton(
                isFirstItemVisible = isFirstItemVisible,
                onClick = {
                    isCreateProjectDialogVisible = !isCreateProjectDialogVisible
                })
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
        if (isCreateProjectDialogVisible) {
            CreateProjectDialog(
                onCreateProject = { projectName ->
                    viewModel.createProject(projectName)
                },
                onDismiss = {
                    isCreateProjectDialogVisible = false
                })
        }
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
            derivedStateOf {
                isScrolling(scrollState.firstVisibleItemScrollOffset < -5)
                isFirstItemVisible(scrollState.firstVisibleItemIndex == 0)
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
private fun CreateProjectFloatingActionButton(
    isFirstItemVisible: Boolean = true,
    onClick: () -> Unit
) {
    if (isFirstItemVisible) {
        ExtendedFloatingActionButton(
            text = {
                Text(text = stringResource(R.string.action_create_project))
            },
            onClick = onClick,
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
            onClick = onClick,
            modifier = Modifier.padding(16.dp),
            content = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            })
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CreateProjectDialog(
    modifier: Modifier = Modifier,
    onCreateProject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var projectName by rememberSaveable { mutableStateOf("") }
    var isCreateClicked by rememberSaveable { mutableStateOf(false) }
    val focusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current
    Dialog(
        onDismissRequest = onDismiss,
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
                    .focusRequester(focusRequester = focusRequester)
                    .focusOrder(focusRequester = focusRequester)
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                label = { Text(stringResource(R.string.field_project_name)) },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusRequester.freeFocus()
                    keyboardController?.hide()
                }),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(textColor = MaterialTheme.colors.onSurface)
            )
            Spacer(modifier = Modifier.height(height = 16.dp))
            Row(modifier = Modifier.align(alignment = Alignment.End)) {
                TextButton(
                    modifier = Modifier
                        .padding(all = 8.dp),
                    onClick = { onDismiss() }) {
                    Text(text = stringResource(R.string.action_dialog_cancel))
                }
                TextButton(
                    modifier = Modifier
                        .focusRequester(focusRequester = focusRequester)
                        .focusOrder(focusRequester = focusRequester)
                        .padding(all = 8.dp),
                    onClick = {
                        isCreateClicked = !isCreateClicked
                        onCreateProject(projectName)
                    }
                ) {
                    Text(text = stringResource(R.string.action_dialog_create))
                }
            }
        }
    }
}

private fun showSnackbar(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    coroutineScope.launch {
        snackbarHostState.showSnackbar(message = message)
    }
}