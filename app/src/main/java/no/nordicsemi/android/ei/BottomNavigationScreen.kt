/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination

/**
 * BottomNavigationScreen
 *
 * @param route Route
 * @param resourceId String resource for the bottom navigation bar item name
 * @param imageVector ImageVector for the icon
 */
enum class BottomNavigationScreen(
    val route: String,
    @StringRes val resourceId: Int,
    val imageVector: ImageVector,
    val shouldFabBeVisible: Boolean,
) {
    DEVICES(
        route = Route.devices,
        resourceId = R.string.label_devices,
        imageVector = Icons.Rounded.DeveloperBoard,
        shouldFabBeVisible = false
    ),

    DATA_ACQUISITION(
        route = Route.dataAcquisition,
        resourceId = R.string.label_data_acquisition,
        imageVector = Icons.Rounded.Storage,
        shouldFabBeVisible = true
    ),

    DEPLOYMENT(
        route = Route.deployment,
        resourceId = R.string.label_deployment,
        imageVector = Icons.Rounded.ViewInAr,
        shouldFabBeVisible = false
    ),

    INFERENCING(
        route = Route.inferencing,
        resourceId = R.string.title_inferencing,
        imageVector = Icons.Rounded.Assessment,
        shouldFabBeVisible = false
    );

    companion object {
        fun fromNav(navDestination: NavDestination) = when (navDestination.route) {
            DEVICES.route -> DEVICES
            DATA_ACQUISITION.route -> DATA_ACQUISITION
            DEPLOYMENT.route -> DEPLOYMENT
            INFERENCING.route -> INFERENCING
            else -> throw IllegalArgumentException("$navDestination.route is not a valid route")
        }
    }
}