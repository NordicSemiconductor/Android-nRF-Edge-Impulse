package no.nordicsemi.android.ei.ui

import android.content.res.Configuration.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.*
import androidx.compose.material.ModalBottomSheetValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
import no.nordicsemi.android.ei.util.asMessage
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel
import no.nordicsemi.android.ei.viewmodels.event.Error

@Composable
fun Project(
    viewModel: ProjectViewModel,
    onBackPressed: () -> Unit
) {
    val isLargeScreen =
        LocalConfiguration.current.screenLayout and SCREENLAYOUT_SIZE_MASK >= SCREENLAYOUT_SIZE_LARGE
    var selectedScreen: BottomNavigationScreen by rememberSaveable { mutableStateOf(BottomNavigationScreen.Devices) }
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
        BottomNavigationScreen.DataAcquisition -> {
            Dialog(
                onDismissRequest = { isDialogVisible = false },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                content = {
                    RecordSampleLargeScreen(
                        connectedDevices = viewModel.configuredDevices,
                        focusRequester = viewModel.focusRequester,
                        selectedDevice = viewModel.selectedDevice,
                        onDeviceSelected = { viewModel.onDeviceSelected(it) },
                        label = viewModel.label,
                        onLabelChanged = { viewModel.onLabelChanged(it) },
                        selectedSensor = viewModel.selectedSensor,
                        onSensorSelected = { viewModel.onSensorSelected(it) },
                        selectedFrequency = viewModel.selectedFrequency,
                        onFrequencySelected = { viewModel.onFrequencySelected(it) },
                        onDismiss = { isDialogVisible = false }
                    )
                }
            )
        }
        else -> {}
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
    val currentOrientation = LocalConfiguration.current.orientation
    val isLandscape = currentOrientation == ORIENTATION_LANDSCAPE
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = Hidden)

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            RecordSampleSmallScreen(
                isLandscape = isLandscape,
                connectedDevices = viewModel.configuredDevices,
                focusRequester = viewModel.focusRequester,
                selectedDevice = viewModel.selectedDevice,
                onDeviceSelected = { viewModel.onDeviceSelected(device = it) },
                label = viewModel.label,
                onLabelChanged = { viewModel.onLabelChanged(label = it) },
                selectedSensor = viewModel.selectedSensor,
                onSensorSelected = { viewModel.onSensorSelected(sensor = it) },
                selectedFrequency = viewModel.selectedFrequency,
                onFrequencySelected = { viewModel.onFrequencySelected(frequency = it) },
                onCloseClicked = {
                    hideBottomSheet(
                        scope = scope,
                        modalBottomSheetState = modalBottomSheetState
                    )
                }
            )
        }
    ) {
        ProjectContent(
            viewModel = viewModel,
            scope = scope,
            selectedScreen = selectedScreen,
            onScreenChanged = onScreenChanged,
            isFabVisible = selectedScreen.shouldFabBeVisible && !modalBottomSheetState.isVisible,
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

@OptIn(ExperimentalPagerApi::class)
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
    val pagerState = rememberPagerState(pageCount = 3)

    LocalLifecycleOwner.current.lifecycleScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect {
                when (it) {
                    is Error -> {
                        showSnackbar(
                            coroutineScope = scope,
                            snackbarHostState = snackbarHostState,
                            message = it.throwable.asMessage(context, context.getString(R.string.error_refreshing_failed))
                        )
                    }
                    else -> {}
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
                    text = stringResource(id = R.string.content_decription_close_record_new_data),
                    expanded = { false },
                    onClick = onFabClicked
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavigationScreen.Devices.route
        ) {
            composable(route = BottomNavigationScreen.Devices.route) { backStackEntry ->
                val devicesViewModel: DevicesViewModel = viewModel(
                    factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
                )
                Devices(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = innerPadding),
                    configuredDevices = viewModel.configuredDevices,
                    refreshingState = viewModel.isRefreshing,
                    onRefresh = { viewModel.listDevices(true) },
                    scannerState = devicesViewModel.scannerState,
                    onScannerStarted = { devicesViewModel.startScan() }
                )
            }
            composable(route = BottomNavigationScreen.DataAcquisition.route) { backStackEntry ->
                val dataAcquisitionViewModel: DataAcquisitionViewModel = viewModel(
                    factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
                )
                DataAcquisition(
                    connectedDevice = viewModel.configuredDevices,
                    pagerState = pagerState,
                    viewModel = dataAcquisitionViewModel,
                    snackbarHostState = snackbarHostState,
                )
            }
            composable(route = BottomNavigationScreen.Deployment.route) {
                Deployment(modifier = Modifier.padding(paddingValues = innerPadding))
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
        HorizontalPagerTab.Training,
        HorizontalPagerTab.Testing,
    )
    when (selectedScreen) {
        BottomNavigationScreen.DataAcquisition -> {
            TabTopAppBar(
                title = { Title(text = projectName) },
                tabs = tabs.map { stringResource(it.title) },
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
    val screens =  listOf(
        BottomNavigationScreen.Devices,
        BottomNavigationScreen.DataAcquisition,
        BottomNavigationScreen.Deployment
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