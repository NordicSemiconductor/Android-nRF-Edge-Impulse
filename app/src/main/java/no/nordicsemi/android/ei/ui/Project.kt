package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import no.nordicsemi.android.ei.BottomNavigationScreen
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel

@Composable
fun Project(
    viewModel: ProjectViewModel,
    bottomNavigationScreens: List<BottomNavigationScreen>
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    IconButton(onClick = {}) {
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
                bottomNavigationScreens = bottomNavigationScreens
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavigationScreen.Devices.route
        ) {
            composable(route = BottomNavigationScreen.Devices.route) {
                Devices(modifier = Modifier.padding(paddingValues = innerPadding))
            }
            composable(route = BottomNavigationScreen.DataAcquisition.route) {
                DataAcquisition(modifier = Modifier.padding(paddingValues = innerPadding))
            }
            composable(route = BottomNavigationScreen.Deployment.route) {
                Deployment(modifier = Modifier.padding(paddingValues = innerPadding))
            }
        }
    }
}

@Composable
fun ProjectBottomNavigationBar(
    navController: NavController,
    bottomNavigationScreens: List<BottomNavigationScreen>
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