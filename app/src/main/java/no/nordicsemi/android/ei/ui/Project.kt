package no.nordicsemi.android.ei.ui

import android.content.res.Configuration.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.ModalBottomSheetValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import no.nordicsemi.android.ei.ui.layouts.CollapsibleFloatingActionButton
import no.nordicsemi.android.ei.ui.layouts.TabTopAppBar
import no.nordicsemi.android.ei.ui.layouts.isScrollingUp
import no.nordicsemi.android.ei.util.asMessage
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.DeploymentViewModel
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel
import no.nordicsemi.android.ei.viewmodels.event.Event
import java.util.*

@Composable
fun Project(
    viewModel: ProjectViewModel,
    onBackPressed: () -> Unit
) {
    val isLargeScreen =
        LocalConfiguration.current.screenLayout and SCREENLAYOUT_SIZE_MASK >= SCREENLAYOUT_SIZE_LARGE
    var selectedScreen: BottomNavigationScreen by rememberSaveable {
        mutableStateOf(
            BottomNavigationScreen.DEVICES
        )
    }

    if (isLargeScreen) {
        LargeScreen(
            viewModel = viewModel,
            selectedScreen = selectedScreen,
            onScreenChanged = { selectedScreen = it },
            onBackPressed = onBackPressed
        )
    } else {
        SmallScreen(
            viewModel = viewModel,
            selectedScreen = selectedScreen,
            onScreenChanged = { selectedScreen = it },
            onBackPressed = onBackPressed
        )
    }
}

@Composable
private fun LargeScreen(
    viewModel: ProjectViewModel,
    selectedScreen: BottomNavigationScreen,
    onScreenChanged: (BottomNavigationScreen) -> Unit,
    onBackPressed: () -> Unit
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    ProjectContent(
        viewModel = viewModel,
        scope = rememberCoroutineScope(),
        selectedScreen = selectedScreen,
        onScreenChanged = onScreenChanged,
        isFabVisible = selectedScreen.shouldFabBeVisible && !isDialogVisible,
        onFabClicked = { isDialogVisible = true },
        onBackPressed = onBackPressed
    )
    if (isDialogVisible) when (selectedScreen) {
        BottomNavigationScreen.DATA_ACQUISITION -> {
            Dialog(
                onDismissRequest = { isDialogVisible = false },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                val connectedDevices by remember { viewModel.connectedDevices }
                RecordSampleLargeScreen(
                    content = {
                        RecordSampleContent(
                            connectedDevices = connectedDevices,
                            focusRequester = viewModel.focusRequester,
                            selectedDevice = viewModel.selectedDevice,
                            onDeviceSelected = { viewModel.onDeviceSelected(device = it) },
                            label = viewModel.label,
                            onLabelChanged = { viewModel.onLabelChanged(label = it) },
                            selectedSensor = viewModel.selectedSensor,
                            onSensorSelected = { viewModel.onSensorSelected(sensor = it) },
                            sampleLength = viewModel.sampleLength,
                            onSampleLengthChanged = { viewModel.onSampleLengthChanged(it) },
                            selectedFrequency = viewModel.selectedFrequency,
                            onFrequencySelected = { viewModel.onFrequencySelected(frequency = it) }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { isDialogVisible = false }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_dialog_cancel).uppercase(
                                        Locale.US
                                    ),
                                    style = MaterialTheme.typography.button
                                )
                            }
                            Spacer(modifier = Modifier.width(32.dp))
                            TextButton(
                                enabled = viewModel.selectedSensor != null,
                                onClick = {
                                    viewModel.selectedDevice?.let { device ->
                                        viewModel.commsManagers[device.deviceId]?.startSampling(
                                            viewModel.label,
                                            10000,
                                            viewModel.selectedFrequency!!.toInt(),
                                            viewModel.selectedSensor!!
                                        )
                                    }
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
            }
        }
        else -> {
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SmallScreen(
    viewModel: ProjectViewModel,
    selectedScreen: BottomNavigationScreen,
    onScreenChanged: (BottomNavigationScreen) -> Unit,
    onBackPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isLandscape = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = Hidden)
    val connectedDevices by remember { viewModel.connectedDevices }
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            RecordSampleSmallScreen(
                isLandscape = isLandscape,
                onCloseClicked = {
                    hideBottomSheet(
                        scope = scope,
                        modalBottomSheetState = modalBottomSheetState
                    )
                },
                content = {
                    RecordSampleContent(
                        connectedDevices = connectedDevices,
                        focusRequester = viewModel.focusRequester,
                        selectedDevice = viewModel.selectedDevice,
                        onDeviceSelected = { viewModel.onDeviceSelected(device = it) },
                        label = viewModel.label,
                        onLabelChanged = { viewModel.onLabelChanged(label = it) },
                        selectedSensor = viewModel.selectedSensor,
                        onSensorSelected = { viewModel.onSensorSelected(sensor = it) },
                        sampleLength = viewModel.sampleLength,
                        onSampleLengthChanged = { viewModel.onSampleLengthChanged(it) },
                        selectedFrequency = viewModel.selectedFrequency,
                        onFrequencySelected = { viewModel.onFrequencySelected(frequency = it) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            enabled = viewModel.selectedSensor != null,
                            onClick = {
                                viewModel.selectedDevice?.let { device ->
                                    viewModel.commsManagers[device.deviceId]?.startSampling(
                                        viewModel.label,
                                        10000,
                                        viewModel.selectedFrequency!!.toInt(),
                                        viewModel.selectedSensor!!
                                    )
                                }
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
        }
    ) {
        ProjectContent(
            viewModel = viewModel,
            scope = scope,
            selectedScreen = selectedScreen,
            onScreenChanged = onScreenChanged,
            isFabVisible = selectedScreen.shouldFabBeVisible,// && !modalBottomSheetState.isVisible,
            onFabClicked = {
                showBottomSheet(
                    scope = scope,
                    modalBottomSheetState = modalBottomSheetState,
                    isLandsScape = isLandscape,
                )
            },
            onBackPressed = onBackPressed
        )
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
private fun ProjectContent(
    viewModel: ProjectViewModel,
    scope: CoroutineScope,
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

    val pagerState = rememberPagerState(pageCount = 2)
    val trainingListState = rememberLazyListState()
    val testingListState = rememberLazyListState()
    val listStates = listOf(trainingListState, testingListState)

    LocalLifecycleOwner.current.lifecycleScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect {
                when (it) {
                    is Event.Error -> {
                        showSnackbar(
                            coroutineScope = scope,
                            snackbarHostState = snackbarHostState,
                            message = it.throwable.asMessage(
                                context,
                                context.getString(R.string.error_refreshing_failed)
                            )
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
                onBackPressed = onBackPressed,
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
            composable(route = BottomNavigationScreen.DEVICES.route) { backStackEntry ->
                val devicesViewModel: DevicesViewModel = viewModel(
                    factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
                )
                Devices(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = innerPadding),
                    configuredDevices = viewModel.configuredDevices,
                    activeDevices = viewModel.commsManagers,
                    refreshingState = viewModel.isRefreshing,
                    onRefresh = { viewModel.listDevices() },
                    scannerState = devicesViewModel.scannerState,
                    onScannerStarted = { devicesViewModel.startScan() },
                    connect = { viewModel.connect(device = it) }
                )
            }
            composable(route = BottomNavigationScreen.DATA_ACQUISITION.route) { backStackEntry ->
                val dataAcquisitionViewModel: DataAcquisitionViewModel = viewModel(
                    factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
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
                    snackbarHostState = snackbarHostState,
                )
            }
            composable(route = BottomNavigationScreen.DEPLOYMENT.route) { backStackEntry ->
                val deploymentViewModel: DeploymentViewModel = viewModel(
                    factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
                )
                val connectedDevices by remember { viewModel.connectedDevices }
                Deployment(
                    deploymentViewModel = deploymentViewModel,
                    connectedDevices = connectedDevices,
                    logs = viewModel.logs
                )
            }
        }
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
                    IconButton(onClick = onBackPressed) {
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
                    IconButton(onClick = onBackPressed) {
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
        BottomNavigationScreen.DEPLOYMENT
    )
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        screens.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.drawableRes),
                        contentDescription = null,
                        modifier = Modifier
                            .requiredSize(32.dp)
                            .padding(8.dp)
                    )
                },
                label = {
                    Text(text = stringResource(id = screen.resourceId))
                },
                selected = currentRoute == screen.route,
                onClick = {
                    if (screen.route != currentRoute) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.startDestinationId)
                            // Avoid multiple copies of the same destination when
                            // re-selecting the same item
                            launchSingleTop = true
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
private fun showBottomSheet(
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    targetValue: ModalBottomSheetValue
) {
    scope.launch {
        modalBottomSheetState.animateTo(targetValue = targetValue)
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun hideBottomSheet(
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState
) {
    scope.launch {
        modalBottomSheetState.hide()
    }
}