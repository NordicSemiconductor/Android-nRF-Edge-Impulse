/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavDestination

/**
 * BottomNavigationScreen
 *
 * @param route Route
 * @param resourceId String resource for the bottom navigation bar item name
 * @param drawableRes ImageVector for the icon
 */
enum class BottomNavigationScreen(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val drawableRes: Int,
    val shouldFabBeVisible: Boolean,
) {
    DEVICES(
        route = Route.devices,
        resourceId = R.string.label_devices,
        drawableRes = R.drawable.ic_round_developer_board_24,
        shouldFabBeVisible = false
    ),

    DATA_ACQUISITION(
        route = Route.dataAcquisition,
        resourceId = R.string.label_data_acquisition,
        drawableRes = R.drawable.ic_round_storage_24,
        shouldFabBeVisible = true
    ),

    DEPLOYMENT(
        route = Route.deployment,
        resourceId = R.string.label_deployment,
        drawableRes = R.drawable.ic_deployment_24,
        shouldFabBeVisible = false
    ),

    INFERENCING(
        route = Route.inferencing,
        resourceId = R.string.title_inferencing,
        drawableRes = R.drawable.ic_outline_inferencing_24,
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