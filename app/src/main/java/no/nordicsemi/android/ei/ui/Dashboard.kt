/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.ei.ui

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Collaborator
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.ui.layouts.UserAppBar
import no.nordicsemi.android.ei.ui.layouts.UserAppBarImageSize
import no.nordicsemi.android.ei.ui.layouts.isScrollingUp
import no.nordicsemi.android.ei.ui.theme.NordicMiddleGrey
import no.nordicsemi.android.ei.viewmodels.DashboardViewModel
import no.nordicsemi.android.ei.viewmodels.event.Event
import java.net.UnknownHostException

@Composable
fun Dashboard(
    viewModel: DashboardViewModel,
    onProjectSelected: (Project) -> Unit,
    onDeleteUser: () -> Unit,
    onLogout: (Unit) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val user = viewModel.user
    val swipeRefreshState = rememberPullToRefreshState()
    val developmentKeysState = viewModel.isDownloadingDevelopmentKeys

    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    var showCreateProjectDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    val isLargeScreen =
        LocalConfiguration.current.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.eventFlow.collect {
                when (it) {
                    is Event.Project.Created -> {
                        showCreateProjectDialog = false
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.project_created_successfully,
                                it.projectName
                            )
                        )
                    }

                    is Event.Project.Selected -> onProjectSelected((it).project)
                    is Event.Error -> {
                        showCreateProjectDialog = false
                        snackbarHostState.showSnackbar(
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            UserAppBar(
                title = stringResource(id = R.string.label_welcome),
                user = user,
                onAboutClick = { showAboutDialog = !showAboutDialog },
                onDeleteUserClick = onDeleteUser,
                onLogoutClick = { onLogout(viewModel.logout()) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .navigationBarsPadding(),
                text = { Text(text = stringResource(R.string.action_create_project)) },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                onClick = { showCreateProjectDialog = !showCreateProjectDialog },
                expanded = lazyListState.isScrollingUp()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .consumeWindowInsets(paddingValues = innerPadding)
                .windowInsetsPadding(
                    insets = WindowInsets.safeDrawing.only(
                        sides = WindowInsetsSides.Horizontal
                    )
                ),
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshUser() },
            content = {
                user.projects.takeIf { it.isNotEmpty() }?.let { notEmptyProjects ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                        /*.padding(innerPadding)*/,
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
                                imageVector = Icons.Outlined.Share,
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
        if (showCreateProjectDialog) {
            CreateProjectDialog(
                onCreateProject = { projectName, projectVisibility ->
                    viewModel.createProject(projectName, projectVisibility)
                },
                onDismiss = { showCreateProjectDialog = !showCreateProjectDialog }
            )
        }
        if (showAboutDialog) {
            AboutDialog(
                onDismiss = { showAboutDialog = !showAboutDialog }
            )
        }

        if (developmentKeysState) {
            ShowDownloadingDevelopmentKeysDialog()
        }
    }
    Surface(
        modifier = with(Modifier) {
            offset(y = (56.dp))
                .padding(
                    start = 16.dp,
                    top = when {
                        isLargeScreen -> 32.dp
                        else -> 56.dp
                    }
                )
                .border(
                    border = BorderStroke(3.dp, color = MaterialTheme.colorScheme.onPrimary),
                    shape = CircleShape
                )
                .height(UserAppBarImageSize)
                .aspectRatio(1.0f)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape
                )
        },
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
    selectable: Boolean = true,
    onProjectSelected: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = selectable) {
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

private const val MAX_COLLABORATOR_IMAGES = 4
private const val MAX_COLLABORATORS = 9