/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.ei.ui

import android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
import android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.common.theme.view.NordicAppBar
import no.nordicsemi.android.ei.BottomNavigationScreen
import no.nordicsemi.android.ei.HorizontalPagerTab
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.Route
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.Message.Sample.Finished
import no.nordicsemi.android.ei.model.Message.Sample.Unknown
import no.nordicsemi.android.ei.ui.layouts.AlertDialog
import no.nordicsemi.android.ei.ui.layouts.TabTopAppBar1
import no.nordicsemi.android.ei.ui.layouts.isScrollingUp
import no.nordicsemi.android.ei.ui.theme.NordicMiddleGrey
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import java.net.UnknownHostException

@Composable
fun Project(
    viewModel: ProjectViewModel,
    onBackPressed: () -> Unit
) {
    val isLargeScreen =
        LocalConfiguration.current.screenLayout and SCREENLAYOUT_SIZE_MASK >= SCREENLAYOUT_SIZE_LARGE
    var selectedScreen by rememberSaveable {
        mutableStateOf(
            BottomNavigationScreen.DEVICES
        )
    }

    val connectedDevices by remember {
        derivedStateOf {
            viewModel.configuredDevices.filter { device ->
                viewModel.commsManagers[device.deviceId]?.connectivityState == DeviceState.AUTHENTICATED
            }
        }
    }

    if (isLargeScreen) {
        LargeScreen(
            viewModel = viewModel,
            connectedDevices = connectedDevices,
            selectedScreen = selectedScreen,
            onScreenChanged = { selectedScreen = it },
            onSamplingMessageDismissed = { viewModel.resetSamplingState() },
            onBackPressed = onBackPressed
        )
    } else {
        SmallScreen(
            viewModel = viewModel,
            connectedDevices = connectedDevices,
            selectedScreen = selectedScreen,
            onScreenChanged = { selectedScreen = it },
            onSamplingMessageDismissed = { viewModel.resetSamplingState() },
            onBackPressed = onBackPressed
        )
    }
}

@Composable
private fun LargeScreen(
    viewModel: ProjectViewModel,
    connectedDevices: List<Device>,
    selectedScreen: BottomNavigationScreen,
    onScreenChanged: (BottomNavigationScreen) -> Unit,
    onSamplingMessageDismissed: (Boolean) -> Unit,
    onBackPressed: () -> Unit
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    ProjectContent(
        viewModel = viewModel,
        connectedDevices = connectedDevices,
        samplingState = viewModel.samplingState,
        isBackHandlerEnabled = false,
        selectedScreen = selectedScreen,
        onScreenChanged = onScreenChanged,
        isSamplingMessageVisible = viewModel.samplingState !is Unknown && !isDialogVisible && !viewModel.isSamplingStartedFromDevice,
        onSamplingMessageDismissed = onSamplingMessageDismissed,
        isFabVisible = selectedScreen.shouldFabBeVisible && !isDialogVisible,
        onFabClicked = { isDialogVisible = true },
        onBackPressed = onBackPressed
    )
    if (isDialogVisible) when (selectedScreen) {
        BottomNavigationScreen.DATA_ACQUISITION -> {
            DataAcquisitionDialog(
                imageVector = Icons.Rounded.Sensors,
                title = stringResource(R.string.title_record_new_data),
                content = {
                    RecordSampleLargeScreen(
                        viewModel = viewModel,
                        connectedDevices = connectedDevices,
                        onSamplingMessageDismissed = onSamplingMessageDismissed
                    )
                },
                isDismissEnabled = viewModel.samplingState is Finished || viewModel.samplingState is Unknown,
                onDismissRequest = {
                    isDialogVisible =
                        !(viewModel.samplingState is Finished || viewModel.samplingState is Unknown)
                    viewModel.resetSamplingState()
                },
                isConfirmEnabled = connectedDevices.isNotEmpty() && viewModel.label.isNotEmpty() &&
                        (viewModel.samplingState is Finished || viewModel.samplingState is Unknown),
                onConfirm = { viewModel.startSampling() },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            )
        }

        else -> {
        }
    }
}

@Composable
private fun SmallScreen(
    viewModel: ProjectViewModel,
    connectedDevices: List<Device>,
    selectedScreen: BottomNavigationScreen,
    onScreenChanged: (BottomNavigationScreen) -> Unit,
    onSamplingMessageDismissed: (Boolean) -> Unit,
    onBackPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val modalBottomSheetState =
        rememberModalBottomSheetState(
            confirmValueChange = {
                if (it == SheetValue.PartiallyExpanded || it == SheetValue.Expanded)
                    true
                else (viewModel.samplingState is Finished || viewModel.samplingState is Unknown)
            }
        )
    val isBackHandlerEnabled by remember {
        derivedStateOf {
            modalBottomSheetState.isVisible
        }
    }

    ProjectContent(
        viewModel = viewModel,
        scope = scope,
        connectedDevices = connectedDevices,
        samplingState = viewModel.samplingState,
        isBackHandlerEnabled = isBackHandlerEnabled,
        selectedScreen = selectedScreen,
        onScreenChanged = onScreenChanged,
        isSamplingMessageVisible = viewModel.samplingState !is Unknown &&
                !viewModel.isSamplingStartedFromDevice,
        onSamplingMessageDismissed = onSamplingMessageDismissed,
        isFabVisible = selectedScreen.shouldFabBeVisible,
        onFabClicked = {
            showBottomSheet = true
        },
        onBackPressed = {
            if (modalBottomSheetState.isVisible &&
                (viewModel.samplingState is Finished || viewModel.samplingState is Unknown)
            ) {
                showBottomSheet = false
            } else onBackPressed()
        }
    )
    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            onDismissRequest = {
                if (viewModel.samplingState is Finished || viewModel.samplingState is Unknown) {
                    hideBottomSheet(scope = scope, bottomSheetState = modalBottomSheetState) {
                        showBottomSheet = false
                    }
                    viewModel.resetSamplingState()
                }
            },
            sheetState = modalBottomSheetState
        ) {
            RecordSampleSmallScreen(
                viewModel = viewModel,
                connectedDevices = connectedDevices,
                onSamplingMessageDismissed = onSamplingMessageDismissed,
                buttonContent = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            enabled = connectedDevices.isNotEmpty() &&
                                    viewModel.label.isNotEmpty() &&
                                    (viewModel.samplingState is Finished ||
                                            viewModel.samplingState is Unknown),
                            onClick = viewModel::startSampling
                        ) {
                            Text(text = stringResource(R.string.action_start_sampling))
                        }
                    }
                },
                onCloseClicked = {
                    if (viewModel.samplingState is Finished || viewModel.samplingState is Unknown) {
                        showBottomSheet = false
                        viewModel.resetSamplingState()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectContent(
    viewModel: ProjectViewModel,
    scope: CoroutineScope = rememberCoroutineScope(),
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    isBackHandlerEnabled: Boolean,
    selectedScreen: BottomNavigationScreen,
    isSamplingMessageVisible: Boolean,
    onSamplingMessageDismissed: (Boolean) -> Unit,
    onScreenChanged: (BottomNavigationScreen) -> Unit,
    isFabVisible: Boolean,
    onFabClicked: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    // TODO: Check why this listener is called twice
    navController.addOnDestinationChangedListener { _, destination, _ ->
        onScreenChanged(BottomNavigationScreen.fromNav(destination))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 },
    )
    val trainingListState = rememberLazyListState()
    val testingListState = rememberLazyListState()
    val listStates = listOf(trainingListState, testingListState)
    val inferencingState by remember { viewModel.inferencingState }
    val inferencingResults by remember {
        viewModel.inferencingResults
    }
    var isWarningDialogVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.eventFlow.collect {
                when (it) {
                    is Event.Error -> {
                        snackbarHostState.showSnackbar(
                            message = when (it.throwable) {
                                is UnknownHostException -> context.getString(R.string.error_no_internet)
                                else -> it.throwable.localizedMessage
                                    ?: context.getString(R.string.error_refreshing_failed)
                            }
                        )
                    }

                    else -> {}
                }
            }
        }
    }
    Scaffold(
        topBar = {
            ProjectTopAppBar(
                /*modifier = Modifier.fillMaxWidth(),*/
                projectName = viewModel.project.name,
                selectedScreen = selectedScreen,
                pagerState = pagerState,
                onBackPressed = {
                    // We should only exit this screen if the backdrop is revealed and no devices
                    // are connected.
                    when (connectedDevices.isNotEmpty()) {
                        true -> isWarningDialogVisible = true
                        false -> onBackPressed()
                    }
                },
            )
        },
        bottomBar = { ProjectBottomNavigation(navController = navController) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (isFabVisible) {
                ExtendedFloatingActionButton(
                    text = { Text(text = stringResource(id = R.string.action_record_new_data)) },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                    onClick = onFabClicked,
                    expanded = listStates[pagerState.currentPage].isScrollingUp()
                )
            }
        }
    ) { innerPadding ->
        val padding = innerPadding.calculateTopPadding()
        Column(modifier = Modifier.padding(top = padding/*, bottom = padding*/)) {
            SamplingMessage(
                isSamplingMessageVisible = isSamplingMessageVisible,
                onSamplingMessageDismissed = onSamplingMessageDismissed,
                samplingState = samplingState,
                isSamplingStartedFromDevice = viewModel.isSamplingStartedFromDevice
            )
            NavHost(
                navController = navController,
                startDestination = BottomNavigationScreen.DEVICES.route
            ) {
                composable(route = BottomNavigationScreen.DEVICES.route) {
                    val devicesViewModel = hiltViewModel<DevicesViewModel>()
                    BackHandler(
                        enabled = selectedScreen == BottomNavigationScreen.DEVICES,
                        onBack = {
                            when (connectedDevices.isNotEmpty()) {
                                true -> isWarningDialogVisible = true
                                false -> onBackPressed()
                            }
                        }
                    )
                    Devices(
                        scope = scope,
                        viewModel = devicesViewModel,
                        modifier = Modifier
                            .fillMaxSize(),
                        configuredDevices = viewModel.configuredDevices,
                        activeDevices = viewModel.commsManagers,
                        refreshingState = viewModel.isRefreshing,
                        onRefresh = viewModel::listDevices,
                        scannerState = devicesViewModel.scannerState,
                        onBluetoothStateChanged = { isEnabled ->
                            if (isEnabled) devicesViewModel.startScan()
                            else devicesViewModel.stopScan()
                        },
                        connect = viewModel::connect,
                        disconnect = viewModel::disconnect,
                        onRenameClick = viewModel::rename,
                        onDeleteClick = viewModel::delete
                    )
                }
                composable(route = BottomNavigationScreen.DATA_ACQUISITION.route) {
                    val dataAcquisitionViewModel = hiltViewModel<DataAcquisitionViewModel>()
                    BackHandler(
                        enabled = isBackHandlerEnabled,
                        onBack = onBackPressed
                    )
                    DataAcquisition(
                        modifier = Modifier.fillMaxSize(),
                        pagerState = pagerState,
                        listStates = listStates,
                        samples = listOf(
                            dataAcquisitionViewModel.trainingSamples,
                            dataAcquisitionViewModel.testingSamples,
                        ),
                        samplingState = samplingState
                    )
                }
                composable(route = BottomNavigationScreen.DEPLOYMENT.route) {
                    Deployment(
                        modifier = Modifier.fillMaxSize(),
                        project = viewModel.project,
                        connectedDevices = connectedDevices,
                        deploymentTarget = viewModel.deploymentTarget,
                        onDeploymentTargetSelected = viewModel::onDeploymentTargetSelected,
                        deploymentState = viewModel.deploymentState,
                        onDeployClick = { viewModel.deploy() },
                        onCancelDeployClick = viewModel::cancelDeploy
                    )
                }
                composable(route = BottomNavigationScreen.INFERENCING.route) {
                    InferencingScreen(
                        modifier = Modifier.fillMaxSize(),
                        connectedDevices = connectedDevices,
                        inferenceResults = inferencingResults,
                        inferencingTarget = viewModel.inferencingTarget,
                        onInferencingTargetSelected = viewModel::onInferencingTargetSelected,
                        inferencingState = inferencingState,
                        sendInferencingRequest = viewModel::sendInferencingRequest
                    )
                }
            }
        }
    }
    if (isWarningDialogVisible) {
        AlertDialog(
            imageVector = Icons.Rounded.Warning,
            title = stringResource(id = R.string.title_warning),
            text = {
                Text(
                    text = stringResource(id = R.string.label_warning_projects),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            dismissText = stringResource(id = R.string.action_cancel),
            onDismiss = {
                isWarningDialogVisible = !isWarningDialogVisible
            },
            confirmText = stringResource(id = R.string.action_continue),
            onConfirm = {
                isWarningDialogVisible = !isWarningDialogVisible
                viewModel.disconnectAllDevices()
                onBackPressed()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ProjectTopAppBar(
    projectName: String,
    selectedScreen: BottomNavigationScreen,
    pagerState: PagerState,
    onBackPressed: () -> Unit
) {
    val tabs = listOf(HorizontalPagerTab.TRAINING, HorizontalPagerTab.TESTING)

    when (selectedScreen) {
        BottomNavigationScreen.DATA_ACQUISITION -> {
            TabTopAppBar1(
                title = { Title(text = projectName) },
                tabs = tabs.map {
                    val text = @Composable {
                        Text(
                            text = stringResource(id = it.title),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    val icon = @Composable {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    text to icon
                },
                pagerState = pagerState,
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }

        else -> {
            NordicAppBar(
                text = projectName,
                onNavigationButtonClick = onBackPressed,
                showBackButton = true
            )
            /*TopAppBar(
                title = { Title(text = projectName) },
                modifier = modifier,
                navigationIcon = {
                    IconButton(onClick = {
                        onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )*/
        }
    }
}

@Composable
private fun Title(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.padding(end = 16.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onPrimary,
    )
}

@Composable
private fun ProjectBottomNavigation(
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Route.devices
    val screens = listOf(
        BottomNavigationScreen.DEVICES,
        BottomNavigationScreen.DATA_ACQUISITION,
        BottomNavigationScreen.DEPLOYMENT,
        BottomNavigationScreen.INFERENCING
    )
    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = screen.imageVector, contentDescription = null)
                },
                label = {
                    Text(
                        text = stringResource(id = screen.resourceId),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                selected = currentRoute == screen.route,
                onClick = {
                    if (screen.route != currentRoute) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // re-selecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }/*,
                selectedContentColor = MaterialTheme.colorScheme.primaryVariant,
                unselectedContentColor = LocalContentColor.current.copy(alpha = 0.6f)*/
            )
        }
    }
}

@Composable
fun SamplingMessage(
    isSamplingMessageVisible: Boolean,
    onSamplingMessageDismissed: (Boolean) -> Unit,
    samplingState: Message.Sample,
    isSamplingStartedFromDevice: Boolean
) {
    if (isSamplingMessageVisible) {
        Surface(modifier = Modifier.wrapContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(color = NordicMiddleGrey)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    text = when (samplingState) {
                        is Unknown -> stringResource(R.string.unknown)
                        is Message.Sample.Request -> {
                            if (isSamplingStartedFromDevice) stringResource(R.string.label_sending_sample_request)
                            else stringResource(R.string.label_ei_sending_sample_request)
                        }

                        is Message.Sample.Response -> {
                            if (isSamplingStartedFromDevice) stringResource(R.string.label_sampling_response_received)
                            else stringResource(R.string.label_ei_sampling_response_received)
                        }

                        is Message.Sample.ProgressEvent.Started -> {
                            if (isSamplingStartedFromDevice) stringResource(R.string.label_sampling_started)
                            else stringResource(R.string.label_ei_sampling_started)
                        }

                        is Message.Sample.ProgressEvent.Processing -> {
                            if (isSamplingStartedFromDevice) stringResource(R.string.label_sampling_processing)
                            else stringResource(R.string.label_ei_sampling_processing)
                        }

                        is Message.Sample.ProgressEvent.Uploading -> {
                            stringResource(R.string.label_uploading_started)
                        }

                        is Finished -> stringResource(R.string.label_sampling_finished).plus(
                            if (samplingState.error != null)
                                ": ${samplingState.error}"
                            else "."
                        )

                        is Message.Sample.ProgressEvent.Reading -> stringResource(R.string.label_sampling_reading)
                    },
                    color = Color.White
                )
                when (samplingState) {
                    is Finished -> {
                        IconButton(
                            onClick = { onSamplingMessageDismissed(false) },
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        )
                    }

                    else -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
