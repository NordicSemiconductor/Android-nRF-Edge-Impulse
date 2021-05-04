package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.BottomNavigationScreen
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.Route
import no.nordicsemi.android.ei.showSnackbar
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel
import no.nordicsemi.android.ei.viewmodels.event.Error
import java.net.UnknownHostException

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Project(
    viewModel: ProjectViewModel,
    bottomNavigationScreens: List<BottomNavigationScreen>,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    var isRecordNewDataFabVisible by rememberSaveable { mutableStateOf(false) }
    LocalLifecycleOwner.current.lifecycleScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect {
                when (it) {
                    is Error -> {
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
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.project.name,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                })
        },
        bottomBar = {
            ProjectBottomNavigationBar(
                navController = navController,
                bottomNavigationScreens = bottomNavigationScreens,
                onDataAcquisitionNotSelected = {
                    if (it) {
                        isRecordNewDataFabVisible = false
                        hideBottomSheet(
                            coroutineScope = coroutineScope,
                            bottomSheetState = bottomSheetState
                        )
                    }
                })
        },
        floatingActionButton = {
            if (isRecordNewDataFabVisible && bottomSheetState.isCollapsed)
                RecordDataFloatingActionButton(onClick = {
                    isRecordNewDataFabVisible = false
                    showBottomSheet(
                        coroutineScope = coroutineScope,
                        bottomSheetState = bottomSheetState
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
                    modifier = Modifier.padding(paddingValues = innerPadding),
                    bottomSheetScaffoldState = bottomSheetScaffoldState,
                    viewModel = dataAcquisitionViewModel,
                    connectedDevices = viewModel.configuredDevices,
                    displayCreateSampleFab = {
                        isRecordNewDataFabVisible = it
                    }
                )
            }
            composable(route = BottomNavigationScreen.Deployment.route) {
                Deployment(modifier = Modifier.padding(paddingValues = innerPadding))
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProjectBottomNavigationBar(
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
    coroutineScope: CoroutineScope,
    bottomSheetState: BottomSheetState
) {
    coroutineScope.launch {
        if (bottomSheetState.isCollapsed) bottomSheetState.expand()
        else bottomSheetState.collapse()
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun hideBottomSheet(
    coroutineScope: CoroutineScope,
    bottomSheetState: BottomSheetState
) {
    coroutineScope.launch {
        if (bottomSheetState.isExpanded) bottomSheetState.collapse()
    }
}

val <T> T.exhaustive: T
    get() = this