package no.nordicsemi.android.ei.ui

import android.content.res.Configuration.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.ModalBottomSheetValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.*
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Category
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.Message.Sample.Finished
import no.nordicsemi.android.ei.model.Message.Sample.Unknown
import no.nordicsemi.android.ei.ui.layouts.CollapsibleFloatingActionButton
import no.nordicsemi.android.ei.ui.layouts.TabTopAppBar
import no.nordicsemi.android.ei.ui.layouts.isScrollingUp
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import okhttp3.internal.filterList
import java.net.UnknownHostException
import java.util.*

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
    val connectedDevices by derivedStateOf {
        viewModel.configuredDevices.filterList {
            viewModel.dataAcquisitionManagers[deviceId]?.state == DeviceState.AUTHENTICATED
        }
    }

    if (isLargeScreen) {
        LargeScreen(
            viewModel = viewModel,
            connectedDevices = connectedDevices,
            selectedScreen = selectedScreen,
            onScreenChanged = { selectedScreen = it },
            onBackPressed = onBackPressed
        )
    } else {
        SmallScreen(
            viewModel = viewModel,
            connectedDevices = connectedDevices,
            selectedScreen = selectedScreen,
            onScreenChanged = { selectedScreen = it },
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
    onBackPressed: () -> Unit
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf(Category.TRAINING) }
    val samplingState by remember { viewModel.samplingState }
    ProjectContent(
        viewModel = viewModel,
        connectedDevices = connectedDevices,
        samplingState = samplingState,
        isBackHandlerEnabled = false,
        selectedScreen = selectedScreen,
        onScreenChanged = onScreenChanged,
        isFabVisible = selectedScreen.shouldFabBeVisible && !isDialogVisible,
        onFabClicked = { isDialogVisible = true },
        onBackPressed = onBackPressed
    )
    if (isDialogVisible) when (selectedScreen) {
        BottomNavigationScreen.DATA_ACQUISITION -> {
            ShowDialog(
                imageVector = Icons.Rounded.Sensors,
                title = stringResource(R.string.title_record_new_data),
                onDismissRequest = { isDialogVisible = false },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                content = {
                    RecordSampleLargeScreen(
                        content = {
                            RecordSampleContent(
                                samplingState = samplingState,
                                connectedDevices = connectedDevices,
                                category = category,
                                onCategorySelected = { category = it },
                                dataAcquisitionTarget = viewModel.dataAcquisitionTarget,
                                onDataAcquisitionTargetSelected = {
                                    viewModel.onDataAcquisitionSelected(
                                        device = it
                                    )
                                },
                                label = viewModel.label,
                                onLabelChanged = { viewModel.onLabelChanged(label = it) },
                                selectedSensor = viewModel.sensor,
                                onSensorSelected = { viewModel.onSensorSelected(sensor = it) },
                                sampleLength = viewModel.sampleLength,
                                onSampleLengthChanged = { viewModel.onSampleLengthChanged(it) },
                                selectedFrequency = viewModel.frequency
                            ) { viewModel.onFrequencySelected(frequency = it) }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    enabled = samplingState is Finished || samplingState is Unknown,
                                    onClick = {
                                        isDialogVisible =
                                            !(samplingState is Finished || samplingState is Unknown)
                                        viewModel.resetSamplingState()
                                    }
                                ) {
                                    Text(
                                        text = stringResource(R.string.action_cancel).uppercase(
                                            Locale.US
                                        ),
                                        style = MaterialTheme.typography.button
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    enabled = connectedDevices.isNotEmpty() && viewModel.label.isNotEmpty() &&
                                            (samplingState is Finished || samplingState is Unknown),
                                    onClick = {
                                        viewModel.startSampling(category = category)
                                    }
                                ) {
                                    Text(
                                        text = stringResource(R.string.action_start_sampling).uppercase(
                                            Locale.US
                                        ),
                                        style = MaterialTheme.typography.button
                                    )
                                }
                            }
                        }
                    )
                })
        }
        else -> {
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SmallScreen(
    viewModel: ProjectViewModel,
    connectedDevices: List<Device>,
    selectedScreen: BottomNavigationScreen,
    onScreenChanged: (BottomNavigationScreen) -> Unit,
    onBackPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isLandscape = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE
    val samplingState by remember { viewModel.samplingState }
    val modalBottomSheetState =
        rememberModalBottomSheetState(
            initialValue = Hidden, confirmStateChange = {
                if (it == HalfExpanded || it == Expanded) true
                else (samplingState is Finished || samplingState is Unknown)
            }
        )
    var category by rememberSaveable { mutableStateOf(Category.TRAINING) }
    val isBackHandlerEnabled by derivedStateOf {
        modalBottomSheetState.isVisible
    }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            RecordSampleSmallScreen(
                content = {
                    RecordSampleContent(
                        samplingState = samplingState,
                        connectedDevices = connectedDevices,
                        category = category,
                        onCategorySelected = { category = it },
                        dataAcquisitionTarget = viewModel.dataAcquisitionTarget,
                        onDataAcquisitionTargetSelected = {
                            viewModel.onDataAcquisitionSelected(
                                device = it
                            )
                        },
                        label = viewModel.label,
                        onLabelChanged = { viewModel.onLabelChanged(label = it) },
                        selectedSensor = viewModel.sensor,
                        onSensorSelected = { viewModel.onSensorSelected(sensor = it) },
                        sampleLength = viewModel.sampleLength,
                        onSampleLengthChanged = { viewModel.onSampleLengthChanged(it) },
                        selectedFrequency = viewModel.frequency
                    ) { viewModel.onFrequencySelected(frequency = it) }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            enabled = connectedDevices.isNotEmpty() && viewModel.label.isNotEmpty() &&
                                    (samplingState is Finished || samplingState is Unknown),
                            onClick = {
                                viewModel.startSampling(category = category)
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.action_start_sampling).uppercase(
                                    Locale.US
                                ),
                                style = MaterialTheme.typography.button
                            )
                        }
                    }
                },
                onCloseClicked = {
                    if (samplingState is Finished || samplingState is Unknown) {
                        hideBottomSheet(
                            scope = scope,
                            modalBottomSheetState = modalBottomSheetState
                        )
                        viewModel.resetSamplingState()
                    }
                }
            )
        }
    ) {
        ProjectContent(
            viewModel = viewModel,
            scope = scope,
            connectedDevices = connectedDevices,
            samplingState = samplingState,
            isBackHandlerEnabled = isBackHandlerEnabled,
            selectedScreen = selectedScreen,
            onScreenChanged = onScreenChanged,
            isFabVisible = selectedScreen.shouldFabBeVisible,
            onFabClicked = {
                showBottomSheet(
                    scope = scope,
                    modalBottomSheetState = modalBottomSheetState,
                    isLandsScape = isLandscape,
                )
            },
            onBackPressed = {
                if (modalBottomSheetState.isVisible && (samplingState is Finished || samplingState is Unknown))
                    hideBottomSheet(scope = scope, modalBottomSheetState = modalBottomSheetState)
                else onBackPressed()
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
private fun ProjectContent(
    viewModel: ProjectViewModel,
    scope: CoroutineScope = rememberCoroutineScope(),
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    isBackHandlerEnabled: Boolean,
    selectedScreen: BottomNavigationScreen,
    onScreenChanged: (BottomNavigationScreen) -> Unit,
    isFabVisible: Boolean,
    onFabClicked: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    // TODO: Check why this listener is called twice
    navController.addOnDestinationChangedListener { _, destination, _ ->
        onScreenChanged(BottomNavigationScreen.fromNav(destination))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(initialPage = 0)
    val trainingListState = rememberLazyListState()
    val testingListState = rememberLazyListState()
    val listStates = listOf(trainingListState, testingListState)
    var isWarningDialogVisible by rememberSaveable { mutableStateOf(false) }
    val inferencingState by remember { viewModel.inferencingState }
    val inferencingResults by remember { viewModel.inferencingResults }

    LocalLifecycleOwner.current.lifecycleScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect { event ->
                when (event) {
                    is Event.Error -> {
                        showSnackbar(
                            coroutineScope = scope,
                            snackbarHostState = snackbarHostState,
                            message = when (event.throwable) {
                                is UnknownHostException -> context.getString(R.string.error_no_internet)
                                else -> event.throwable.localizedMessage
                                    ?: context.getString(R.string.error_refreshing_failed)
                            }
                        )
                    }
                    else -> {
                    }
                }
            }
        }
    }
    Scaffold(
        scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState),
        topBar = {
            ProjectTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                projectName = viewModel.project.name,
                selectedScreen = selectedScreen,
                pagerState = pagerState,
                onBackPressed = {
                    when (connectedDevices.isNotEmpty()) {
                        true -> isWarningDialogVisible = true
                        false -> onBackPressed()
                    }
                },
            )
        },
        bottomBar = {
            ProjectBottomNavigation(
                navController = navController,
            )
        },
        floatingActionButton = {
            if (isFabVisible) {
                CollapsibleFloatingActionButton(
                    imageVector = Icons.Default.Add,
                    text = stringResource(id = R.string.action_record_new_data),
                    expanded = {
                        val currentListState = listStates[pagerState.currentPage]
                        currentListState.isScrollingUp()
                    },
                    onClick = onFabClicked
                )
            }
        }
    ) { innerPadding ->
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
                        .fillMaxSize()
                        .padding(paddingValues = innerPadding),
                    configuredDevices = viewModel.configuredDevices,
                    activeDevices = viewModel.dataAcquisitionManagers,
                    refreshingState = viewModel.isRefreshing,
                    onRefresh = { viewModel.listDevices() },
                    scannerState = devicesViewModel.scannerState,
                    onScannerStarted = { devicesViewModel.startScan() },
                    screen = selectedScreen,
                    connect = { viewModel.connect(device = it) },
                    disconnect = { viewModel.disconnect(device = it) },
                    onRenameClick = { device, name ->
                        viewModel.rename(
                            device = device,
                            name = name
                        )
                    },
                    onDeleteClick = { viewModel.delete(it) }
                )
            }
            composable(route = BottomNavigationScreen.DATA_ACQUISITION.route) {
                val dataAcquisitionViewModel = hiltViewModel<DataAcquisitionViewModel>()
                BackHandler(
                    enabled = isBackHandlerEnabled,
                    onBack = onBackPressed
                )
                DataAcquisition(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = innerPadding),
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
                    project = viewModel.project,
                    connectedDevices = connectedDevices,
                    deploymentTarget = viewModel.deploymentTarget,
                    onDeploymentTargetSelected = { viewModel.onDeploymentTargetSelected(it) },
                    deploymentState = viewModel.deploymentState,
                    onDeployClick = { viewModel.deploy() },
                    progress = viewModel.progress,
                    transferSpeed = viewModel.transferSpeed,
                    onCancelDeployClick = { viewModel.cancelDeploy() }
                )
            }
            composable(route = BottomNavigationScreen.INFERENCING.route) {
                InferencingScreen(
                    connectedDevices = connectedDevices,
                    inferenceResults = inferencingResults,
                    inferencingTarget = viewModel.inferencingTarget,
                    onInferencingTargetSelected = { viewModel.onInferencingTargetSelected(it) },
                    inferencingState = inferencingState
                ) { inferencingRequest ->
                    viewModel.sendInferencingRequest(
                        inferencingRequest = inferencingRequest
                    )
                }
            }
        }
    }
    if (isWarningDialogVisible) {
        ShowDialog(
            imageVector = Icons.Rounded.Warning,
            title = stringResource(R.string.title_warning),
            onDismissRequest = { isWarningDialogVisible = !isWarningDialogVisible },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            content = {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = stringResource(R.string.label_warning_projects),
                    style = MaterialTheme.typography.subtitle1
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { isWarningDialogVisible = !isWarningDialogVisible }
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel).uppercase(
                                Locale.US
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            isWarningDialogVisible = !isWarningDialogVisible
                            viewModel.disconnectAllDevices()
                            onBackPressed()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_dialog_continue).uppercase(
                                Locale.US
                            )
                        )
                    }
                }
            })
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ProjectTopAppBar(
    modifier: Modifier = Modifier,
    projectName: String,
    selectedScreen: BottomNavigationScreen,
    pagerState: PagerState,
    onBackPressed: () -> Unit
) {
    val tabs = listOf(
        HorizontalPagerTab.TRAINING,
        HorizontalPagerTab.TESTING,
    )

    when (selectedScreen) {
        BottomNavigationScreen.DATA_ACQUISITION -> {
            TabTopAppBar(
                title = { Title(text = projectName) },
                tabs = tabs.map {
                    val text = @Composable {
                        Text(text = stringResource(id = it.title).uppercase(Locale.US))
                    }
                    val icon = @Composable {
                        Icon(
                            painter = rememberVectorPainter(image = it.icon),
                            contentDescription = null
                        )
                    }
                    text to icon
                },
                pagerState = pagerState,
                modifier = modifier,
                navigationIcon = {
                    IconButton(onClick = {
                        onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
        else -> {
            TopAppBar(
                title = { Title(text = projectName) },
                modifier = modifier,
                navigationIcon = {
                    IconButton(onClick = {
                        onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
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
        overflow = TextOverflow.Ellipsis
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
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        screens.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.drawableRes),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = screen.resourceId), /*overflow = TextOverflow.Ellipsis,*/
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
                },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = LocalContentColor.current
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun showBottomSheet(
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    isLandsScape: Boolean,
) {
    showBottomSheet(
        scope = scope,
        modalBottomSheetState = modalBottomSheetState,
        targetValue = if (isLandsScape) Expanded else HalfExpanded
    )
}

@OptIn(ExperimentalMaterialApi::class)
fun showBottomSheet(
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    targetValue: ModalBottomSheetValue
) {
    scope.launch {
        modalBottomSheetState.animateTo(targetValue = targetValue)
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun hideBottomSheet(
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState
) {
    scope.launch {
        modalBottomSheetState.hide()
    }
}
