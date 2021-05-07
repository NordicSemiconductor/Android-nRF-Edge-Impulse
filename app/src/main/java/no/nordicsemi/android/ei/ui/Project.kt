package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ModalBottomSheetValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.*
import no.nordicsemi.android.ei.R
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
    bottomNavigationScreens: List<BottomNavigationScreen>,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val pages = remember {
        listOf(
            HorizontalPagerTab.Training, HorizontalPagerTab.Testing, HorizontalPagerTab.Anomaly
        )
    }
    val pagerState = rememberPagerState(pageCount = pages.size)
    var currentState by rememberSaveable { mutableStateOf(Hidden) }
    var targetState by rememberSaveable { mutableStateOf(Hidden) }
    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = Hidden,
        confirmStateChange = {
            it != HalfExpanded
        }
    )
    val connectedDevice = viewModel.configuredDevices
    var isRecordNewDataFabVisible by rememberSaveable { mutableStateOf(false) }
    var showDataAcquisitionTitle by rememberSaveable { mutableStateOf(false) }

    if (targetState == HalfExpanded) {
        hideBottomSheet(scope, modalBottomSheetState)
        targetState = Hidden
    }
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
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            RecordSample(
                connectedDevices = connectedDevice,
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
        Scaffold(
            scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState),
            topBar = {
                ProjectTopAppBar(
                    projectName = viewModel.project.name,
                    pages = pages,
                    pagerState = pagerState,
                    showDataAcquisitionTitle = showDataAcquisitionTitle,
                    onBackPressed = onBackPressed
                )
            },
            bottomBar = {
                ProjectBottomNavigationBar(
                    navController = navController,
                    bottomNavigationScreens = bottomNavigationScreens,
                    onDataAcquisitionNotSelected = {
                        if (it) {
                            showDataAcquisitionTitle = false
                            isRecordNewDataFabVisible = false
                            hideBottomSheet(
                                scope = scope,
                                modalBottomSheetState = modalBottomSheetState
                            )
                        } else {
                            showDataAcquisitionTitle = true
                        }
                    })
            },
            floatingActionButton = {
                if (isRecordNewDataFabVisible)
                    RecordDataFloatingActionButton(onClick = {
                        isRecordNewDataFabVisible = false
                        showBottomSheet(
                            scope = scope,
                            modalBottomSheetState = modalBottomSheetState
                        )
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
                        configuredDevices = connectedDevice,
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
                    if (!modalBottomSheetState.isVisible) {
                        isRecordNewDataFabVisible = true
                    }
                    DataAcquisition(
                        connectedDevice = viewModel.configuredDevices,
                        modalBottomSheetState = modalBottomSheetState,
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
}

@ExperimentalPagerApi
@Composable
private fun ProjectTopAppBar(
    projectName: String,
    pages: List<HorizontalPagerTab>,
    pagerState: PagerState,
    showDataAcquisitionTitle: Boolean,
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
            if (showDataAcquisitionTitle)
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
    bottomNavigationScreens: List<BottomNavigationScreen>,
    onDataAcquisitionNotSelected: (Boolean) -> Unit
) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)
        bottomNavigationScreens.forEach { screen ->
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
                    onDataAcquisitionNotSelected(screen.route != Route.dataAcquisition)
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo = navController.graph.startDestination
                        // Avoid multiple copies of the same destination when
                        // re-selecting the same item
                        launchSingleTop = true
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
    modalBottomSheetState: ModalBottomSheetState
) {
    scope.launch {
        if (!modalBottomSheetState.isVisible) {
            modalBottomSheetState.animateTo(Expanded)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun hideBottomSheet(
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState
) {
    if (modalBottomSheetState.isVisible)
        scope.launch {
            modalBottomSheetState.hide()
        }
}

val <T> T.exhaustive: T
    get() = this