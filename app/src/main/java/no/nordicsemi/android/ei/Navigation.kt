/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.ei.ui.Dashboard
import no.nordicsemi.android.ei.ui.DeleteUser
import no.nordicsemi.android.ei.ui.Login
import no.nordicsemi.android.ei.ui.Project

@Composable
fun Navigation(
    onCancelled: () -> Unit = {},
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.login) {
        composable(Route.login) {
            Login(
                viewModel = hiltViewModel(),
                onLoggedIn = {
                    navController.navigate(Route.dashboard) {
                        popUpTo(Route.login) {
                            inclusive = true
                        }
                    }
                },
                onCancelled = onCancelled,
                onError = { onCancelled() },
            )
        }

        composable(Route.dashboard) {
            Dashboard(
                viewModel = hiltViewModel(),
                onProjectSelected = {
                    navController.navigate(Route.project)
                },
                onDeleteUser = {
                    navController.navigate(Route.deleteUser)
                },
                onLogout = {
                    navController.navigate(Route.login) {
                        popUpTo(Route.dashboard) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Route.deleteUser) {
            DeleteUser(
                viewModel = hiltViewModel(),
                onDeleted = {
                    navController.navigate(Route.login) {
                        popUpTo(Route.dashboard) {
                            inclusive = true
                        }
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.project) {
            Project(
                viewModel = hiltViewModel(),
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
