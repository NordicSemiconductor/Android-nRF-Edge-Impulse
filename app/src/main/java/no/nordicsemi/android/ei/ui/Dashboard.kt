package no.nordicsemi.android.ei.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.collect
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Collaborator
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.showSnackbar
import no.nordicsemi.android.ei.ui.layouts.SwipeToRefreshLayout
import no.nordicsemi.android.ei.ui.layouts.UserAppBar
import no.nordicsemi.android.ei.ui.theme.NordicMiddleGrey
import no.nordicsemi.android.ei.viewmodels.DashboardViewModel
import no.nordicsemi.android.ei.viewmodels.event.DismissDialog
import no.nordicsemi.android.ei.viewmodels.event.Error
import no.nordicsemi.android.ei.viewmodels.event.ProjectCreated
import no.nordicsemi.android.ei.viewmodels.event.ProjectSelected
import java.net.UnknownHostException

@Composable
fun Dashboard(
    viewModel: DashboardViewModel,
    onProjectSelected: () -> Unit,
    onLogout: (Unit) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = LocalLifecycleOwner.current.lifecycleScope

    val user = viewModel.user
    val refreshState = viewModel.isRefreshing
    val developmentKeysState = viewModel.isDownloadingDevelopmentKeys

    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    var isCreateProjectDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isScrollingUp by remember { mutableStateOf(false) }

    coroutineScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect { event ->
                when (event) {
                    is DismissDialog -> {
                        isCreateProjectDialogVisible = false
                    }
                    is ProjectCreated -> {
                        showSnackbar(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            message = context.getString(
                                R.string.project_created_successfully,
                                event.projectName
                            )
                        )
                    }
                    is ProjectSelected -> {
                        onProjectSelected()
                    }
                    is Error -> {
                        showSnackbar(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            message = when (event.throwable) {
                                is UnknownHostException -> context.getString(R.string.error_no_internet)
                                else -> event.throwable.localizedMessage
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
                isScrollingUp = {
                    isScrollingUp = lazyListState.isScrollingUp()
                    isScrollingUp
                },
                onClick = {
                    isCreateProjectDialogVisible = !isCreateProjectDialogVisible
                })
        }
    ) { innerPadding ->
        SwipeToRefreshLayout(
            refreshingState = refreshState,
            onRefresh = {
                if (isScrollingUp)
                    viewModel.refreshUser()
            },
            refreshIndicator = {
                if (isScrollingUp)
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
                user.projects.takeIf { it.isNotEmpty() }?.let { notEmptyProjects ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(top = 72.dp, bottom = 36.dp),
                        state = lazyListState
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
                            ProjectRow(
                                project = project,
                                onProjectSelected = {
                                    viewModel.selectProject(
                                        project = project
                                    )
                                })
                            Divider(modifier = Modifier.width(Dp.Hairline))
                        }
                    }
                } ?: run {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
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

        if (developmentKeysState) {
            ShowDownloadingDevelopmentKeysDialog()
        }
    }
}

@Composable
fun ProjectRow(
    modifier: Modifier = Modifier,
    project: Project,
    onProjectSelected: () -> Unit
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colors.surface)
            .clickable {
                onProjectSelected()
            }
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .weight(1.0f),
            text = buildAnnotatedString {
                append(project.owner)
                append(" / ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(project.name)
                }
            },
            fontSize = 18.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(16.dp))
        Collaborator(collaborators = project.collaborators)
    }
}

@Composable
private fun Collaborator(collaborators: List<Collaborator>) {
    var startPadding = 0.dp
    val imageSize = 48.dp
    val maxImages = when (collaborators.size) {
        in 0..MAX_COLLABORATOR_IMAGES -> MAX_COLLABORATOR_IMAGES
        else -> MAX_COLLABORATOR_IMAGES - 1
    }
    Box {
        for ((index, collaborator) in collaborators.take(MAX_COLLABORATOR_IMAGES).withIndex()) {
            Box(
                modifier = Modifier
                    .padding(start = startPadding)
                    .requiredSize(imageSize)
                    .clip(CircleShape)
            ) {
                // lets limit the images to max collaborators
                if (index in 0 until maxImages) {
                    CoilImage(
                        modifier = Modifier.requiredSize(imageSize),
                        data = if (collaborator.photo.isNotBlank()) {
                            collaborator.photo
                        } else {
                            Image(
                                imageVector = Icons.Outlined.AccountCircle,
                                modifier = Modifier.requiredSize(imageSize + 8.dp),
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds
                            )
                        },
                        contentDescription = null,
                        error = {
                            Image(
                                imageVector = Icons.Outlined.AccountCircle,
                                modifier = Modifier
                                    .requiredSize(size = imageSize)
                                    .background(color = MaterialTheme.colors.onSurface),
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds
                            )
                        },
                        loading = {
                            Image(
                                imageVector = Icons.Outlined.AccountCircle,
                                modifier = Modifier.requiredSize(imageSize),
                                contentDescription = null,
                                alpha = 0.1f,
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    )
                } else {
                    Text(
                        text = when (collaborators.size - MAX_COLLABORATOR_IMAGES) {
                            in 0..MAX_COLLABORATOR_IMAGES -> "${collaborators.size - MAX_COLLABORATOR_IMAGES + 1}"
                            else -> "${MAX_COLLABORATORS}+"
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .background(color = NordicMiddleGrey)
                            .padding(8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            startPadding += (imageSize / MAX_COLLABORATOR_IMAGES)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CreateProjectFloatingActionButton(
    isScrollingUp: @Composable () -> Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            // Toggle the visibility of the content with animation.
            AnimatedVisibility(visible = isScrollingUp()) {
                Text(
                    text = stringResource(R.string.action_create_project),
                    modifier = Modifier
                        .padding(start = 8.dp, top = 3.dp)
                )
            }
        }
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

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
private fun ShowDownloadingDevelopmentKeysDialog(
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = {},
        properties =
        DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = modifier
                .width(width = 280.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_baseline_hourglass_top),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.label_please_wait),
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.h6
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.label_fetching_development_keys),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body1
            )
        }
    }
}

private const val MAX_COLLABORATOR_IMAGES = 4
private const val MAX_COLLABORATORS = 9
