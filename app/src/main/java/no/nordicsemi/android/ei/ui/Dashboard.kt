/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ShowDialog
import no.nordicsemi.android.ei.model.Collaborator
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.showSnackbar
import no.nordicsemi.android.ei.ui.layouts.CollapsibleFloatingActionButton
import no.nordicsemi.android.ei.ui.layouts.UserAppBar
import no.nordicsemi.android.ei.ui.layouts.UserAppBarImageSize
import no.nordicsemi.android.ei.ui.layouts.isScrollingUp
import no.nordicsemi.android.ei.ui.theme.NordicMiddleGrey
import no.nordicsemi.android.ei.viewmodels.DashboardViewModel
import no.nordicsemi.android.ei.viewmodels.event.Event
import java.net.UnknownHostException
import java.util.Locale

@Composable
fun Dashboard(
    viewModel: DashboardViewModel,
    onProjectSelected: (Project) -> Unit,
    onLogout: (Unit) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = LocalLifecycleOwner.current.lifecycleScope

    val user = viewModel.user
    val swipeRefreshState = rememberSwipeRefreshState(viewModel.isRefreshing)
    val developmentKeysState = viewModel.isDownloadingDevelopmentKeys

    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    var isCreateProjectDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isAboutDialogVisible by rememberSaveable { mutableStateOf(false) }

    coroutineScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect { event ->
                when (event) {
                    is Event.Project.Created -> {
                        isCreateProjectDialogVisible = false
                        showSnackbar(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            message = context.getString(
                                R.string.project_created_successfully,
                                event.projectName
                            )
                        )
                    }

                    is Event.Project.Selected -> {
                        onProjectSelected(event.project)
                    }

                    is Event.Error -> {
                        isCreateProjectDialogVisible = false
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
        topBar = {
            UserAppBar(
                title = {
                    Text(text = stringResource(id = R.string.label_welcome))
                },
                user = user,
                onAboutClick = { isAboutDialogVisible = !isAboutDialogVisible },
                onLogoutClick = {
                    onLogout(viewModel.logout())
                },
                elevation = 0.dp
            )
        },
        floatingActionButton = {
            CollapsibleFloatingActionButton(
                imageVector = Icons.Default.Add,
                text = stringResource(R.string.action_create_project),
                expanded = { lazyListState.isScrollingUp() },
                onClick = {
                    isCreateProjectDialogVisible = !isCreateProjectDialogVisible
                }
            )
        }
    ) { innerPadding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshUser() },
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
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge
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
                                }
                            )
                            HorizontalDivider()
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
                        CompositionLocalProvider(
                            value = LocalContentColor provides MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_project_diagram),
                                contentDescription = null,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.label_no_projects),
                                style = MaterialTheme.typography.titleLarge
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
                    isCreateProjectDialogVisible = !isCreateProjectDialogVisible
                }
            )
        }
        if (isAboutDialogVisible) {
            ShowAboutDialog(
                onDismiss = {
                    isAboutDialogVisible = !isAboutDialogVisible
                }
            )
        }

        if (developmentKeysState) {
            ShowDownloadingDevelopmentKeysDialog()
        }
    }

    Surface(
        modifier = Modifier
            .offset(y = UserAppBarImageSize / 2)
            .padding(start = 16.dp)
            .border(
                border = BorderStroke(3.dp, color = MaterialTheme.colorScheme.onPrimary),
                shape = CircleShape
            )
            .height(UserAppBarImageSize)
            .aspectRatio(1.0f)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape
            ),
        shape = CircleShape,
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(data = user.photo?.let { photo ->
                    when {
                        photo.isNotBlank() -> photo
                        else -> Image(
                            modifier = Modifier.border(
                                border = BorderStroke(
                                    10.dp,
                                    color = MaterialTheme.colorScheme.surface.copy(0.6f)
                                )
                            ),
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.onSurface.copy(
                                    0.6f
                                )
                            )
                        )
                    }
                } ?: Image(
                    modifier = Modifier.border(
                        border = BorderStroke(
                            3.dp,
                            color = MaterialTheme.colorScheme.surface.copy(0.6f)
                        )
                    ),
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(0.6f))
                )).apply(block = fun ImageRequest.Builder.() {
                    crossfade(true)
                    placeholder(R.drawable.ic_baseline_account_circle_24)
                    transformations(CircleCropTransformation())
                }).build()
            ),
            contentDescription = stringResource(R.string.content_description_user_image),
            alignment = Alignment.Center,
        )
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
            .background(MaterialTheme.colorScheme.surface)
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

@OptIn(ExperimentalCoilApi::class)
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
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(
                                    data = if (collaborator.photo.isNotBlank()) {
                                        collaborator.photo
                                    } else {
                                        Image(
                                            imageVector = Icons.Filled.AccountCircle,
                                            modifier = Modifier.requiredSize(imageSize + 8.dp),
                                            contentDescription = null,
                                            contentScale = ContentScale.FillBounds,
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                            alpha = 0.6f
                                        )
                                    }
                                ).apply(block = fun ImageRequest.Builder.() {
                                    crossfade(true)
                                    placeholder(R.drawable.ic_outline_account_circle_24)
                                    transformations(CircleCropTransformation())
                                }).build()
                        ),
                        contentDescription = null,
                        modifier = Modifier.requiredSize(imageSize),
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
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            startPadding += (imageSize / MAX_COLLABORATOR_IMAGES)
        }
    }
}

@Composable
private fun CreateProjectDialog(
    onCreateProject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isError by rememberSaveable { mutableStateOf(false) }
    var projectName by rememberSaveable { mutableStateOf("") }
    var isCreateClicked by rememberSaveable { mutableStateOf(false) }
    ShowDialog(
        imageVector = Icons.Outlined.Share,
        title = stringResource(id = R.string.dialog_title_create_project),
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isCreateClicked,
            dismissOnClickOutside = !isCreateClicked
        ), content = {
            Column {
                Text(
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    text = stringResource(R.string.label_enter_project_name),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(state = rememberScrollState())
                        .weight(weight = 1.0f, fill = false)
                ) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = {
                            projectName = it
                            isError = projectName.isBlank()
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester = focusRequester)
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
                        label = { Text(stringResource(R.string.field_project_name)) },
                        isError = isError,
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = false,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusRequester.freeFocus()
                            keyboardController?.hide()
                        }),
                        singleLine = true,
                        /*colors = TextFieldDefaults.outlinedTextFieldColors(textColor = MaterialTheme.colorScheme.onSurface)*/
                    )
                    if (isError) {
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = stringResource(R.string.label_empty_project_name_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(height = 16.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onDismiss() }) {
                        Text(text = stringResource(R.string.action_cancel).uppercase(Locale.US))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        modifier = Modifier
                            .focusRequester(focusRequester = focusRequester),
                        enabled = projectName.isNotBlank(),
                        onClick = {
                            isCreateClicked = !isCreateClicked
                            onCreateProject(projectName)
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_create)
                                .uppercase(Locale.US)
                        )
                    }
                }
            }
        })
}

@Composable
private fun ShowAboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    ShowDialog(
        imageVector = Icons.Outlined.Info,
        title = stringResource(id = R.string.action_about),
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        content = {
            Column {
                Row(modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = stringResource(R.string.label_version)
                    )
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = context.packageManager.getPackageInfo(
                            context.packageName,
                            0
                        ).versionName,
                        textAlign = TextAlign.End
                    )
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(text = stringResource(R.string.action_ok))
                }
            }
        }
    )
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
                .background(MaterialTheme.colorScheme.surface)
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
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.label_please_wait),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.label_fetching_development_keys_socket_token),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private const val MAX_COLLABORATOR_IMAGES = 4
private const val MAX_COLLABORATORS = 9
