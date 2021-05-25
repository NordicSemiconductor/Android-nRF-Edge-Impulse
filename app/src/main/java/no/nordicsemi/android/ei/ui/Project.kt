package no.nordicsemi.android.ei.ui

import android.content.res.Configuration.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ModalBottomSheetValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.*
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel
import no.nordicsemi.android.ei.viewmodels.event.Error
import java.net.UnknownHostException

@ExperimentalPagerApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Project(
    viewModel: ProjectViewModel,
    onBackPressed: () -> Unit
) {
    val isLargeScreen =
        LocalConfiguration.current.screenLayout and SCREENLAYOUT_SIZE_MASK >= SCREENLAYOUT_SIZE_LARGE
    var dataAcquisitionSelected by rememberSaveable { mutableStateOf(false) }
    if (isLargeScreen) {
        LargeScreen(
            viewModel = viewModel,
            dataAcquisitionSelected = dataAcquisitionSelected,
            onDataAcquisitionSelected = {
                dataAcquisitionSelected = it
            },
            onBackPressed = onBackPressed
        )
    } else {
        SmallScreen(
            viewModel = viewModel,
            dataAcquisitionSelected = dataAcquisitionSelected,
            onDataAcquisitionSelected = {
                dataAcquisitionSelected = it
            },
            onBackPressed = onBackPressed
        )
    }
}


@OptIn(ExperimentalMaterialApi::class)
@ExperimentalPagerApi
@Composable
private fun LargeScreen(
    viewModel: ProjectViewModel,
    dataAcquisitionSelected: Boolean,
    onDataAcquisitionSelected: (Boolean) -> Unit,
    onBackPressed: () -> Unit
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    ProjectContent(
        viewModel = viewModel,
        scope = rememberCoroutineScope(),
        showDataAcquisitionTitle = dataAcquisitionSelected,
        onDataAcquisitionSelected = {
            onDataAcquisitionSelected(it)
        },
        showFab = dataAcquisitionSelected && !isDialogVisible,
        onClick = {
            isDialogVisible = true
        },
        onBackPressed = onBackPressed
    )
    if (isDialogVisible) {
        ShowDialog(
            configuredDevices = viewModel.configuredDevices,
            focusRequester = viewModel.focusRequester,
            selectedDevice = viewModel.selectedDevice,
            onDeviceSelected = { viewModel.onDeviceSelected(device = it) },
            label = viewModel.label,
            onLabelChanged = { viewModel.onLabelChanged(label = it) },
            selectedSensor = viewModel.selectedSensor,
            onSensorSelected = { viewModel.onSensorSelected(sensor = it) },
            selectedFrequency = viewModel.selectedFrequency,
            onFrequencySelected = { viewModel.onFrequencySelected(frequency = it) },
            dismiss = {
                isDialogVisible = false
            })
    }
}

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalPagerApi
@Composable
private fun SmallScreen(
    viewModel: ProjectViewModel,
    dataAcquisitionSelected: Boolean,
    onDataAcquisitionSelected: (Boolean) -> Unit,
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
        }) {
        ProjectContent(
            viewModel = viewModel,
            scope = scope,
            showDataAcquisitionTitle = dataAcquisitionSelected,
            onDataAcquisitionSelected = {
                onDataAcquisitionSelected(it)
            },
            showFab = dataAcquisitionSelected && !modalBottomSheetState.isVisible,
            onClick = {
                showBottomSheet(
                    isLandsScape = isLandscape,
                    scope = scope,
                    modalBottomSheetState = modalBottomSheetState
                )
            },
            onBackPressed = onBackPressed
        )

    }
}


@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ShowDialog(
    configuredDevices: List<Device>,
    focusRequester: FocusRequester,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    label: String,
    onLabelChanged: (String) -> Unit,
    selectedSensor: Device.Sensor?,
    onSensorSelected: (Device.Sensor) -> Unit,
    selectedFrequency: Number?,
    onFrequencySelected: (Number) -> Unit,
    dismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            dismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ), content = {
            RecordSampleLargeScreen(
                connectedDevices = configuredDevices,
                focusRequester = focusRequester,
                selectedDevice = selectedDevice,
                onDeviceSelected = { onDeviceSelected(it) },
                label = label,
                onLabelChanged = { onLabelChanged(it) },
                selectedSensor = selectedSensor,
                onSensorSelected = { onSensorSelected(it) },
                selectedFrequency = selectedFrequency,
                onFrequencySelected = { onFrequencySelected(it) },
                onDismiss = dismiss
            )
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalPagerApi
@Composable
private fun ProjectContent(
    viewModel: ProjectViewModel,
    scope: CoroutineScope,
    showDataAcquisitionTitle: Boolean,
    onDataAcquisitionSelected: (Boolean) -> Unit,
    showFab: Boolean,
    onClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val pages = remember {
        listOf(
            HorizontalPagerTab.Training, HorizontalPagerTab.Testing, HorizontalPagerTab.Anomaly
        )
    }
    val pagerState = rememberPagerState(pageCount = pages.size)

    LocalLifecycleOwner.current.lifecycleScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect {
                when (it) {
                    is Error -> {
                        showSnackbar(
                            coroutineScope = scope,
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
            ProjectTopAppBar(
                projectName = viewModel.project.name,
                pages = pages,
                pagerState = pagerState,
                isDataAcquisitionSelected = showDataAcquisitionTitle,
                onBackPressed = onBackPressed
            )
        },
        bottomBar = {
            ProjectBottomNavigationBar(
                navController = navController,
                onDataAcquisitionSelected = {
                    onDataAcquisitionSelected(it)
                })
        },
        floatingActionButton = {
            if (showFab)
                RecordDataFloatingActionButton(onClick = {
                    onClick()
                })
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
                    modifier = Modifier.padding(paddingValues = innerPadding),
                    viewModel = devicesViewModel,
                    configuredDevices = viewModel.configuredDevices,
                    refreshingState = viewModel.isRefreshing,
                    onRefresh = {
                        viewModel.listDevices(true)
                    }
                )
            }
            composable(route = BottomNavigationScreen.DataAcquisition.route) { backStackEntry ->
                val dataAcquisitionViewModel: DataAcquisitionViewModel = viewModel(
                    factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
                )
                DataAcquisition(
                    connectedDevice = viewModel.configuredDevices,
                    pagerState = pagerState,
                    pages = pages,
                    viewModel = dataAcquisitionViewModel
                )
            }
            composable(route = BottomNavigationScreen.Deployment.route) {
                Deployment(modifier = Modifier.padding(paddingValues = innerPadding))
            }
        }
    }
}

@ExperimentalPagerApi
@Composable
private fun ProjectTopAppBar(
    projectName: String,
    pages: List<HorizontalPagerTab>,
    pagerState: PagerState,
    isDataAcquisitionSelected: Boolean,
    onBackPressed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colors.primarySurface,
        elevation = AppBarDefaults.TopAppBarElevation
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    onBackPressed()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onPrimary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            if (isDataAcquisitionSelected)
                Row(
                    modifier = Modifier
                        .height(56.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    TabRow(
                        // Selected tab is the current page
                        selectedTabIndex = pagerState.currentPage,
                        // Override the indicator, using the provided pagerTabIndicatorOffset modifier
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                            )
                        }
                    ) {
                        // Adds tabs for all of pages
                        pages.forEachIndexed { index, tab ->
                            Tab(
                                text = { Text(stringResource(id = tab.title)) },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(
                                            index
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProjectBottomNavigationBar(
    navController: NavController,
    onDataAcquisitionSelected: (Boolean) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Route.devices
    val bottomNavigationScreens = remember {
        listOf(
            BottomNavigationScreen.Devices,
            BottomNavigationScreen.DataAcquisition,
            BottomNavigationScreen.Deployment
        )
    }
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        bottomNavigationScreens.forEach { screen ->
            if(currentRoute == screen.route){
                onDataAcquisitionSelected(screen.route == Route.dataAcquisition)
            }
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
                    Text(
                        text = stringResource(id = screen.resourceId)
                    )
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
    isLandsScape: Boolean,
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    targetValue: ModalBottomSheetValue = if (isLandsScape) {
        Expanded
    } else HalfExpanded
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

val <T> T.exhaustive: T
    get() = this